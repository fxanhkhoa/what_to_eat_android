package com.fxanhkhoa.what_to_eat_android.utils

import android.content.Context
import android.util.Log
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import com.auth0.jwt.JWT
import com.auth0.jwt.exceptions.JWTDecodeException
import java.util.Date

class TokenManager private constructor(private val context: Context) {

    companion object {
        @Volatile
        private var INSTANCE: TokenManager? = null

        fun getInstance(context: Context): TokenManager {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: TokenManager(context.applicationContext).also { INSTANCE = it }
            }
        }

        // DataStore instance
        private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "auth_tokens")

        // Keys for stored tokens
        private val ACCESS_TOKEN_KEY = stringPreferencesKey("access_token")
        private val REFRESH_TOKEN_KEY = stringPreferencesKey("refresh_token")
        private val USER_ID_KEY = stringPreferencesKey("user_id")
        private val USER_EMAIL_KEY = stringPreferencesKey("user_email")
        private val USER_NAME_KEY = stringPreferencesKey("user_name")
        private val USER_PHOTO_URL_KEY = stringPreferencesKey("user_photo_url")
        private val TOKEN_EXPIRY_KEY = stringPreferencesKey("token_expiry")
    }

    /**
     * Save authentication tokens and user info
     */
    suspend fun saveTokens(
        accessToken: String,
        refreshToken: String?,
        userId: String? = null,
        userEmail: String? = null,
        userName: String? = null,
        userPhotoUrl: String? = null,
        expiryTime: Long? = null
    ) {
        try {
            Log.d("TokenManager", "Saving tokens - AccessToken: ${accessToken.take(20)}...")
            context.dataStore.edit { preferences ->
                preferences[ACCESS_TOKEN_KEY] = accessToken
                refreshToken?.let {
                    preferences[REFRESH_TOKEN_KEY] = it
                    Log.d("TokenManager", "Saved refresh token")
                }
                userId?.let {
                    preferences[USER_ID_KEY] = it
                    Log.d("TokenManager", "Saved user ID: $it")
                }
                userEmail?.let {
                    preferences[USER_EMAIL_KEY] = it
                    Log.d("TokenManager", "Saved user email: $it")
                }
                userName?.let {
                    preferences[USER_NAME_KEY] = it
                    Log.d("TokenManager", "Saved user name: $it")
                }
                userPhotoUrl?.let {
                    preferences[USER_PHOTO_URL_KEY] = it
                    Log.d("TokenManager", "Saved user photo URL")
                }
                expiryTime?.let {
                    preferences[TOKEN_EXPIRY_KEY] = it.toString()
                    Log.d("TokenManager", "Saved expiry time: $it")
                }
            }
            Log.d("TokenManager", "Tokens saved successfully")

            // Verify the save operation
            val savedToken = getAccessToken()
            Log.d("TokenManager", "Verification - Retrieved token: ${savedToken?.take(20)}...")
        } catch (e: Exception) {
            Log.e("TokenManager", "Error saving tokens", e)
            throw e
        }
    }

    /**
     * Get access token
     */
    suspend fun getAccessToken(): String? {
        return try {
            val token = context.dataStore.data.first()[ACCESS_TOKEN_KEY]
            Log.d("TokenManager", "Retrieved access token: ${token?.take(20) ?: "null"}...")
            token
        } catch (e: Exception) {
            Log.e("TokenManager", "Error getting access token", e)
            null
        }
    }

    /**
     * Get refresh token
     */
    suspend fun getRefreshToken(): String? {
        return context.dataStore.data.first()[REFRESH_TOKEN_KEY]
    }

    /**
     * Get stored user information
     */
    suspend fun getUserInfo(): UserInfo? {
        val preferences = context.dataStore.data.first()
        val userId = preferences[USER_ID_KEY]
        val email = preferences[USER_EMAIL_KEY]
        val name = preferences[USER_NAME_KEY]
        val photoUrl = preferences[USER_PHOTO_URL_KEY]

        return if (userId != null || email != null) {
            UserInfo(
                id = userId,
                email = email,
                name = name,
                photoUrl = photoUrl
            )
        } else null
    }

    /**
     * Get token expiry time
     */
    suspend fun getTokenExpiry(): Long? {
        return context.dataStore.data.first()[TOKEN_EXPIRY_KEY]?.toLongOrNull()
    }

    /**
     * Check if access token is expired
     */
    suspend fun isTokenExpired(): Boolean {
        val accessToken = getAccessToken() ?: return true

        return try {
            // Parse the JWT and get the expiry time
            val expiry = JWT.decode(accessToken).expiresAt
            // Check if the current time is past the expiry time
            expiry?.let { Date().after(it) } ?: true
        } catch (e: JWTDecodeException) {
            // If there's an error decoding the token, consider it expired
            true
        }
    }

    /**
     * Check if user has valid tokens
     */
    suspend fun hasValidTokens(): Boolean {
        val accessToken = getAccessToken()
        Log.d("TokenManager", "Access Token: $accessToken")
        return !accessToken.isNullOrEmpty() && !isTokenExpired()
    }

    /**
     * Flow to observe access token changes
     */
    fun getAccessTokenFlow(): Flow<String?> {
        return context.dataStore.data.map { preferences ->
            preferences[ACCESS_TOKEN_KEY]
        }
    }

    /**
     * Flow to observe if user is authenticated
     */
    fun getIsAuthenticatedFlow(): Flow<Boolean> {
        return context.dataStore.data.map { preferences ->
            !preferences[ACCESS_TOKEN_KEY].isNullOrEmpty()
        }
    }

    /**
     * Update only the access token (useful for token refresh)
     */
    suspend fun updateAccessToken(accessToken: String, expiryTime: Long? = null) {
        context.dataStore.edit { preferences ->
            preferences[ACCESS_TOKEN_KEY] = accessToken
            expiryTime?.let { preferences[TOKEN_EXPIRY_KEY] = it.toString() }
        }
    }

    /**
     * Clear all stored tokens and user data
     */
    suspend fun clearTokens() {
        context.dataStore.edit { preferences ->
            preferences.remove(ACCESS_TOKEN_KEY)
            preferences.remove(REFRESH_TOKEN_KEY)
            preferences.remove(USER_ID_KEY)
            preferences.remove(USER_EMAIL_KEY)
            preferences.remove(USER_NAME_KEY)
            preferences.remove(USER_PHOTO_URL_KEY)
            preferences.remove(TOKEN_EXPIRY_KEY)
        }
    }

    /**
     * Get authorization header for API calls
     */
    suspend fun getAuthorizationHeader(): String? {
        val accessToken = getAccessToken()
        return if (!accessToken.isNullOrEmpty()) {
            "Bearer $accessToken"
        } else null
    }

    /**
     * Check if refresh token exists
     */
    suspend fun hasRefreshToken(): Boolean {
        return !getRefreshToken().isNullOrEmpty()
    }
}

/**
 * Data class for user information
 */
data class UserInfo(
    val id: String? = null,
    val email: String? = null,
    val name: String? = null,
    val photoUrl: String? = null
)

/**
 * Composable helper to get TokenManager instance
 */


@Composable
fun rememberTokenManager(): TokenManager {
    val context = LocalContext.current
    return remember { TokenManager.getInstance(context) }
}
