package com.fxanhkhoa.what_to_eat_android.network

import android.util.Log
import com.fxanhkhoa.what_to_eat_android.data.dto.RefreshTokenRequest
import com.fxanhkhoa.what_to_eat_android.utils.TokenManager
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.Request
import okhttp3.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class AuthInterceptor(private val tokenManager: TokenManager) : Interceptor {

    companion object {
        private const val TAG = "AuthInterceptor"
        private const val AUTHORIZATION_HEADER = "Authorization"
        private const val BEARER_PREFIX = "Bearer "
    }

    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()
        val url = originalRequest.url.toString()

        // Skip adding token for auth endpoints that don't need it
        if (shouldSkipAuth(url)) {
            Log.d(TAG, "Skipping auth for URL: $url")
            return chain.proceed(originalRequest)
        }

        return runBlocking {
            try {
                val accessToken = tokenManager.getAccessToken()

                if (accessToken.isNullOrEmpty()) {
                    Log.w(TAG, "No access token available, proceeding without auth: $url")
                    return@runBlocking chain.proceed(originalRequest)
                }

                // Proactively refresh if expired before making the request
                val tokenToUse = if (tokenManager.isTokenExpired()) {
                    Log.d(TAG, "Access token expired, refreshing proactively...")
                    val refreshToken = tokenManager.getRefreshToken()
                    if (!refreshToken.isNullOrEmpty()) {
                        val refreshed = performRefresh(refreshToken)
                        if (refreshed) {
                            tokenManager.getAccessToken() ?: accessToken
                        } else {
                            Log.w(TAG, "Proactive refresh failed, using stale token")
                            accessToken
                        }
                    } else {
                        Log.w(TAG, "No refresh token, using stale access token")
                        accessToken
                    }
                } else {
                    accessToken
                }

                val response = chain.proceed(originalRequest.withBearer(tokenToUse))

                // On 401: try one token refresh + retry
                if (response.code == 401) {
                    Log.w(TAG, "Received 401, attempting token refresh and retry")
                    response.close()

                    val refreshToken = tokenManager.getRefreshToken()
                    if (!refreshToken.isNullOrEmpty()) {
                        val refreshed = performRefresh(refreshToken)
                        if (refreshed) {
                            val newToken = tokenManager.getAccessToken()
                            if (!newToken.isNullOrEmpty()) {
                                Log.d(TAG, "Retrying request with new access token")
                                return@runBlocking chain.proceed(originalRequest.withBearer(newToken))
                            }
                        }
                    }
                    // Refresh failed — return a fresh 401 so the caller can sign out
                    Log.w(TAG, "401 retry failed, returning unauthorized")
                    return@runBlocking chain.proceed(originalRequest)
                }

                response
            } catch (e: Exception) {
                Log.e(TAG, "Error in AuthInterceptor", e)
                chain.proceed(originalRequest)
            }
        }
    }

    /**
     * Calls the refresh-token endpoint directly (without going through AuthViewModel
     * to avoid circular dependencies) and persists the new tokens.
     */
    private suspend fun performRefresh(refreshToken: String): Boolean {
        return try {
            // Build a bare Retrofit client with no interceptors to avoid infinite loops
            val api = Retrofit.Builder()
                .baseUrl(com.fxanhkhoa.what_to_eat_android.BuildConfig.API_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
                .create(AuthApiService::class.java)

            val response = api.refreshToken(RefreshTokenRequest(refreshToken))
            if (response.isSuccessful && response.body() != null) {
                val body = response.body()!!
                tokenManager.updateAccessToken(
                    accessToken = body.accessToken,
                    newRefreshToken = body.refreshToken
                )
                Log.d(TAG, "Interceptor token refresh succeeded")
                true
            } else {
                Log.w(TAG, "Interceptor token refresh failed: ${response.code()} ${response.message()}")
                false
            }
        } catch (e: Exception) {
            Log.e(TAG, "Interceptor token refresh exception: ${e.message}")
            false
        }
    }

    private fun Request.withBearer(token: String): Request =
        newBuilder().header(AUTHORIZATION_HEADER, "$BEARER_PREFIX$token").build()

    private fun shouldSkipAuth(url: String): Boolean {
        val skipAuthEndpoints = listOf(
            "/auth/login",
            "/auth/refresh-token",   // ← was "/auth/refresh" (wrong path)
            "/auth/google"
        )
        return skipAuthEndpoints.any { url.contains(it, ignoreCase = true) }
    }
}
