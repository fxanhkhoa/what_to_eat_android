package com.fxanhkhoa.what_to_eat_android.services

import android.util.Log
import com.fxanhkhoa.what_to_eat_android.BuildConfig
import com.fxanhkhoa.what_to_eat_android.data.dto.LoginResponse
import com.fxanhkhoa.what_to_eat_android.data.dto.RefreshTokenResponse
import com.fxanhkhoa.what_to_eat_android.data.dto.User
import com.fxanhkhoa.what_to_eat_android.network.AuthApiService
import com.fxanhkhoa.what_to_eat_android.network.AuthInterceptor
import com.fxanhkhoa.what_to_eat_android.utils.TokenManager
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

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
            val response = authApi.login(
                com.fxanhkhoa.what_to_eat_android.data.dto.LoginRequest(
                    token
                )
            )
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
            Log.d("AuthService", "getProfile response code: ${response.code()}")
            Log.d("AuthService", "getProfile response body: ${response.body()}")
            Log.d("AuthService", "getProfile raw response: ${response.raw()}")

            if (response.isSuccessful && response.body() != null) {
                val user = response.body()!!
                Log.d("AuthService", "Parsed User object: id=${user.id}, name=${user.name}, email=${user.email}")
                Result.success(user)
            } else {
                Log.e("AuthService", "Failed to get profile: ${response.message()}, errorBody: ${response.errorBody()?.string()}")
                Result.failure(Exception("Failed to get profile: ${response.message()}"))
            }
        } catch (e: Exception) {
            Log.e("AuthService", "Exception in getProfile: ${e.message}", e)
            Result.failure(e)
        }
    }


    /**
     * Refresh access token
     */
    suspend fun refreshToken(refreshToken: String): Result<RefreshTokenResponse> {
        return try {
            val response = authApi.refreshToken(
                com.fxanhkhoa.what_to_eat_android.data.dto.RefreshTokenRequest(
                    refreshToken
                )
            )
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
            val response = authApi.logout(
                com.fxanhkhoa.what_to_eat_android.data.dto.RefreshTokenRequest(
                    refreshToken
                )
            )
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