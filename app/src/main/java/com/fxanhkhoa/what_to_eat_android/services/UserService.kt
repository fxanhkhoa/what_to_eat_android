package com.fxanhkhoa.what_to_eat_android.services

import android.content.Context
import android.util.Log
import com.fxanhkhoa.what_to_eat_android.model.CreateUserDto
import com.fxanhkhoa.what_to_eat_android.model.QueryUserDto
import com.fxanhkhoa.what_to_eat_android.model.UpdateUserDto
import com.fxanhkhoa.what_to_eat_android.model.UserModel
import com.fxanhkhoa.what_to_eat_android.network.RetrofitProvider
import com.fxanhkhoa.what_to_eat_android.utils.TokenManager
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import retrofit2.Response
import retrofit2.http.*
import java.util.concurrent.ConcurrentHashMap

/**
 * User Service
 * Handles user-related API operations with caching and token refresh
 */
class UserService private constructor(private val context: Context) {

    companion object {
        private const val TAG = "UserService"
        private const val PREFIX = "user"

        @Volatile
        private var INSTANCE: UserService? = null

        fun getInstance(context: Context): UserService {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: UserService(context.applicationContext).also { INSTANCE = it }
            }
        }
    }

    // Dependencies
    private val tokenManager = TokenManager.getInstance(context)
    private val api = RetrofitProvider.createService<UserApiService>()
    private val gson = Gson()

    // Cache for actual user data (thread-safe)
    private val userDataCache = ConcurrentHashMap<String, UserModel>()

    // Cache for ongoing requests to prevent duplicate API calls
    private val ongoingRequests = ConcurrentHashMap<String, Flow<Result<UserModel>>>()

    // Mutex for cache operations
    private val cacheMutex = Mutex()

    init {
        // Note: Clear cache when tokens are cleared
        // This would need to be implemented with a TokenManager observer
        Log.i(TAG, "UserService initialized")
    }

    // MARK: - API Interface

    interface UserApiService {
        @GET("$PREFIX/{id}")
        suspend fun findOne(@Path("id") id: String): Response<UserModel>

        @POST(PREFIX)
        suspend fun create(@Body user: CreateUserDto): Response<UserModel>

        @PUT("$PREFIX/{id}")
        suspend fun update(
            @Path("id") id: String,
            @Body user: UpdateUserDto
        ): Response<UserModel>

        @DELETE("$PREFIX/{id}")
        suspend fun delete(@Path("id") id: String): Response<Unit>

        @POST("$PREFIX/query")
        suspend fun query(@Body queryDto: QueryUserDto): Response<List<UserModel>>
    }

    // MARK: - Public Methods

    /**
     * Find a user by ID with caching
     */
    fun findOne(id: String): Flow<Result<UserModel>> = flow {
        // First check if we have cached data
        userDataCache[id]?.let { cachedUser ->
            Log.d(TAG, "Returning cached user data for ID: $id")
            emit(Result.success(cachedUser))
            return@flow
        }

        // Check if request is already in progress
        ongoingRequests[id]?.let { ongoingRequest ->
            Log.d(TAG, "Returning ongoing request for ID: $id")
            ongoingRequest.collect { emit(it) }
            return@flow
        }

        // Create new request
        Log.i(TAG, "Fetching user with ID: $id")

        val publisher = flow {
            try {
                val response = api.findOne(id)

                if (response.isSuccessful && response.body() != null) {
                    val user = response.body()!!
                    Log.i(TAG, "Successfully fetched user: ${user.email}")

                    // Cache the actual user data
                    cacheMutex.withLock {
                        userDataCache[id] = user
                    }

                    emit(Result.success(user))
                } else {
                    val error = Exception("Failed to fetch user: ${response.message()}")
                    Log.e(TAG, "Failed to fetch user with ID $id: ${response.message()}")
                    emit(Result.failure(error))
                }
            } catch (e: Exception) {
                Log.e(TAG, "Failed to fetch user with ID $id: ${e.message}", e)
                emit(Result.failure(e))
            } finally {
                // Remove from ongoing requests after completion
                cacheMutex.withLock {
                    ongoingRequests.remove(id)
                }
                Log.d(TAG, "Completed user request for ID: $id")
            }
        }.flowOn(Dispatchers.IO)

        // Cache the ongoing request to prevent duplicates
        ongoingRequests[id] = publisher

        publisher.collect { emit(it) }
    }.flowOn(Dispatchers.IO)

    /**
     * Create a new user
     */
    suspend fun create(user: CreateUserDto): Result<UserModel> = withContext(Dispatchers.IO) {
        try {
            Log.i(TAG, "Creating new user with email: ${user.email}")

            val response = api.create(user)

            if (response.isSuccessful && response.body() != null) {
                val createdUser = response.body()!!
                Log.i(TAG, "Successfully created user: ${createdUser.email}")

                // Clear cache to ensure fresh data
                clearCache()

                Result.success(createdUser)
            } else {
                val error = Exception("Failed to create user: ${response.message()}")
                Log.e(TAG, "Failed to create user: ${response.message()}")
                Result.failure(error)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to create user: ${e.message}", e)
            Result.failure(e)
        }
    }

    /**
     * Update an existing user
     */
    suspend fun update(user: UpdateUserDto): Result<UserModel> = withContext(Dispatchers.IO) {
        try {
            Log.i(TAG, "Updating user with ID: ${user.id}")

            val response = api.update(user.id, user)

            if (response.isSuccessful && response.body() != null) {
                val updatedUser = response.body()!!
                Log.i(TAG, "Successfully updated user: ${updatedUser.email}")

                // Remove from cache to ensure fresh data on next request
                cacheMutex.withLock {
                    userDataCache.remove(user.id)
                }

                Result.success(updatedUser)
            } else {
                val error = Exception("Failed to update user: ${response.message()}")
                Log.e(TAG, "Failed to update user with ID ${user.id}: ${response.message()}")
                Result.failure(error)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to update user with ID ${user.id}: ${e.message}", e)
            Result.failure(e)
        }
    }

    /**
     * Delete a user by ID
     */
    suspend fun delete(id: String): Result<Boolean> = withContext(Dispatchers.IO) {
        try {
            Log.i(TAG, "Deleting user with ID: $id")

            val response = api.delete(id)

            Log.d(TAG, "Delete response status: ${response.code()}")

            if (response.isSuccessful) {
                Log.i(TAG, "Successfully deleted user with ID: $id")

                // Remove from cache
                cacheMutex.withLock {
                    userDataCache.remove(id)
                }

                Result.success(true)
            } else {
                val error = Exception("Failed to delete user: ${response.message()}")
                Log.e(TAG, "Failed to delete user with ID $id: ${response.message()}")
                Result.failure(error)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to delete user with ID $id: ${e.message}", e)
            Result.failure(e)
        }
    }

    /**
     * Query users with filters
     */
    suspend fun query(queryDto: QueryUserDto): Result<List<UserModel>> = withContext(Dispatchers.IO) {
        try {
            Log.i(TAG, "Querying users with keyword: ${queryDto.keyword}")

            val response = api.query(queryDto)

            if (response.isSuccessful && response.body() != null) {
                val users = response.body()!!
                Log.i(TAG, "Successfully queried ${users.size} users")
                Result.success(users)
            } else {
                val error = Exception("Failed to query users: ${response.message()}")
                Log.e(TAG, "Failed to query users: ${response.message()}")
                Result.failure(error)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to query users: ${e.message}", e)
            Result.failure(e)
        }
    }

    // MARK: - Cache Management

    /**
     * Clear the user cache
     */
    fun clearCache() {
        userDataCache.clear()
        Log.i(TAG, "User cache cleared")
    }

    /**
     * Remove specific user from cache
     */
    fun removeCachedUser(id: String) {
        userDataCache.remove(id)
        Log.d(TAG, "Removed user $id from cache")
    }

    /**
     * Check if user is cached
     */
    fun isUserCached(id: String): Boolean {
        return userDataCache.containsKey(id)
    }

    // MARK: - Convenience Methods

    /**
     * Get current user profile (requires user ID from auth context)
     */
    suspend fun getCurrentUser(): Result<UserModel?> = withContext(Dispatchers.IO) {
        try {
            val userInfo = tokenManager.getUserInfo()

            if (userInfo?.id != null) {
                // Fetch user data by ID
                findOne(userInfo.id).first()
            } else {
                Log.w(TAG, "getCurrentUser called but no user ID available")
                Result.success(null)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to get current user: ${e.message}", e)
            Result.failure(e)
        }
    }

    /**
     * Refresh user data (bypass cache)
     */
    fun refreshUser(id: String): Flow<Result<UserModel>> = flow {
        // Remove from cache first
        removeCachedUser(id)
        // Then fetch fresh data
        findOne(id).collect { emit(it) }
    }.flowOn(Dispatchers.IO)

    /**
     * Get cached user or fetch if not available
     */
    suspend fun getCachedUserOrFetch(id: String): Result<UserModel> {
        return userDataCache[id]?.let { cachedUser ->
            Log.d(TAG, "Returning cached user for ID: $id")
            Result.success(cachedUser)
        } ?: run {
            Log.d(TAG, "Cache miss, fetching user for ID: $id")
            findOne(id).first()
        }
    }
}
