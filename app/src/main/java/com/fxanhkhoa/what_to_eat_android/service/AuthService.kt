package com.fxanhkhoa.what_to_eat_android.service

import User
import com.fxanhkhoa.what_to_eat_android.BuildConfig
import com.fxanhkhoa.what_to_eat_android.data.model.*
import com.fxanhkhoa.what_to_eat_android.network.AuthApiService
import com.fxanhkhoa.what_to_eat_android.network.AuthInterceptor
import com.fxanhkhoa.what_to_eat_android.utils.TokenManager
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor

class AuthService(private val tokenManager: TokenManager) {
    private val authApi: AuthApiService

    init {
        val loggingInterceptor = HttpLoggingInterceptor().apply {
            level = if (BuildConfig.DEBUG) {
                HttpLoggingInterceptor.Level.BODY
            } else {
                HttpLoggingInterceptor.Level.NONE
            }
        }

        // Create AuthInterceptor with TokenManager
        val authInterceptor = AuthInterceptor(tokenManager)

        val okHttpClient = OkHttpClient.Builder()
            .addInterceptor(loggingInterceptor)
            .addInterceptor(authInterceptor) // Add auth interceptor
            .build()

        val retrofit = Retrofit.Builder()
            .baseUrl(BuildConfig.API_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        authApi = retrofit.create(AuthApiService::class.java)
    }

    /**
     * Login with Google ID token
     */
    suspend fun login(token: String): Result<LoginResponse> {
        return try {
            val response = authApi.login(LoginRequest(token))
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Login failed: ${response.message()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Get user profile from backend
     */
    suspend fun getProfile(): Result<User> {
        return try {
            val response = authApi.getProfile()
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Failed to get profile: ${response.message()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Refresh access token
     */
    suspend fun refreshToken(refreshToken: String): Result<RefreshTokenResponse> {
        return try {
            val response = authApi.refreshToken(RefreshTokenRequest(refreshToken))
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Token refresh failed: ${response.message()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Logout user
     */
    suspend fun logout(refreshToken: String): Result<Unit> {
        return try {
            val response = authApi.logout(RefreshTokenRequest(refreshToken))
            if (response.isSuccessful) {
                Result.success(Unit)
            } else {
                Result.failure(Exception("Logout failed: ${response.message()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}