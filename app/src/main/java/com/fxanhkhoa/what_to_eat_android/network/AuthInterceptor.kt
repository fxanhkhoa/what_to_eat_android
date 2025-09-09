package com.fxanhkhoa.what_to_eat_android.network

import android.util.Log
import com.fxanhkhoa.what_to_eat_android.utils.TokenManager
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.Response

class AuthInterceptor(private val tokenManager: TokenManager) : Interceptor {

    companion object {
        private const val TAG = "AuthInterceptor"
        private const val AUTHORIZATION_HEADER = "Authorization"
        private const val BEARER_PREFIX = "Bearer "
    }

    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()

        // Skip adding token for login endpoints
        val url = originalRequest.url.toString()
        if (shouldSkipAuth(url)) {
            Log.d(TAG, "Skipping auth for URL: $url")
            return chain.proceed(originalRequest)
        }

        return runBlocking {
            try {
                // Get access token
                val accessToken = tokenManager.getAccessToken()

                if (accessToken.isNullOrEmpty()) {
                    Log.w(TAG, "No access token available for request: $url")
                    return@runBlocking chain.proceed(originalRequest)
                }

                // Check if token is expired and try to refresh if possible
                if (tokenManager.isTokenExpired()) {
                    Log.d(TAG, "Access token is expired, attempting refresh...")

                    val refreshToken = tokenManager.getRefreshToken()
                    if (!refreshToken.isNullOrEmpty()) {
                        // Token refresh should be handled by AuthViewModel
                        // For now, we'll proceed with the expired token and let the API handle it
                        Log.w(TAG, "Token is expired but proceeding with request")
                    } else {
                        Log.w(TAG, "No refresh token available, proceeding without auth")
                        return@runBlocking chain.proceed(originalRequest)
                    }
                }

                // Add Authorization header
                val authenticatedRequest = originalRequest.newBuilder()
                    .addHeader(AUTHORIZATION_HEADER, "$BEARER_PREFIX$accessToken")
                    .build()

                Log.d(TAG, "Added Authorization header to request: ${authenticatedRequest.url}")

                val response = chain.proceed(authenticatedRequest)

                // Handle 401 Unauthorized responses
                if (response.code == 401) {
                    Log.w(TAG, "Received 401 Unauthorized response")
                    // Here you could trigger token refresh or logout
                    // For now, we'll just log and return the response
                }

                response

            } catch (e: Exception) {
                Log.e(TAG, "Error in AuthInterceptor", e)
                // If there's an error getting the token, proceed without auth
                chain.proceed(originalRequest)
            }
        }
    }

    /**
     * Check if the request URL should skip authentication
     */
    private fun shouldSkipAuth(url: String): Boolean {
        val skipAuthEndpoints = listOf(
            "/auth/login",
            "/auth/refresh",
            "/auth/google"
        )

        return skipAuthEndpoints.any { endpoint ->
            url.contains(endpoint, ignoreCase = true)
        }
    }
}
