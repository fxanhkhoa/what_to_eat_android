package com.fxanhkhoa.what_to_eat_android.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fxanhkhoa.what_to_eat_android.data.dto.User
import com.fxanhkhoa.what_to_eat_android.services.AuthService
import com.fxanhkhoa.what_to_eat_android.utils.TokenManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class AuthViewModel private constructor(private val tokenManager: TokenManager) : ViewModel() {

    companion object {
        @Volatile
        private var INSTANCE: AuthViewModel? = null

        fun getInstance(tokenManager: TokenManager): AuthViewModel {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: AuthViewModel(tokenManager).also { INSTANCE = it }
            }
        }

        // Method to clear the instance if needed (useful for testing or app reset)
        fun clearInstance() {
            synchronized(this) {
                INSTANCE = null
            }
        }
    }

    private val authService = AuthService(tokenManager)

    private val _isLoggedIn = MutableStateFlow(false)
    val isLoggedIn: StateFlow<Boolean> = _isLoggedIn.asStateFlow()

    private val _user = MutableStateFlow<User?>(null)
    val user: StateFlow<User?> = _user.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    init {
        // Check for existing valid tokens on initialization
        Log.d("AuthViewModel", "Initializing AuthViewModel")
        viewModelScope.launch {
            checkExistingSession()
        }
    }

    /**
     * Check if user has existing valid session
     */
    private suspend fun checkExistingSession() {
        try {
            val accessToken = tokenManager.getAccessToken()
            if (accessToken.isNullOrEmpty()) {
                Log.d("AuthViewModel", "No access token found, user not logged in")
                return
            }

            // Access token exists but may be expired — try refresh first
            if (tokenManager.isTokenExpired()) {
                Log.d("AuthViewModel", "Access token expired, attempting refresh before profile fetch")
                val refreshed = tryRefreshToken()
                if (!refreshed) {
                    Log.w("AuthViewModel", "Token refresh failed, clearing session")
                    tokenManager.clearTokens()
                    _isLoggedIn.value = false
                    _user.value = null
                    return
                }
                Log.d("AuthViewModel", "Token refreshed successfully, fetching profile")
            }

            // Token is valid — fetch profile
            authService.getProfile().fold(
                onSuccess = { user ->
                    Log.d("AuthViewModel", "Profile fetched successfully: $user")
                    val currentUserInfo = tokenManager.getUserInfo()
                    if (currentUserInfo?.id == null) {
                        val newAccessToken = tokenManager.getAccessToken()
                        val newRefreshToken = tokenManager.getRefreshToken()
                        tokenManager.saveTokens(
                            accessToken = newAccessToken ?: "",
                            refreshToken = newRefreshToken,
                            userId = user.id,
                            userName = user.name,
                            userEmail = user.email,
                            userPhotoUrl = user.avatar
                        )
                    }
                    _user.value = user
                    _isLoggedIn.value = true
                },
                onFailure = { exception ->
                    Log.e("AuthViewModel", "Profile fetch failed: ${exception.message}")
                    tokenManager.clearTokens()
                    _isLoggedIn.value = false
                    _user.value = null
                }
            )
        } catch (e: Exception) {
            Log.e("AuthViewModel", "Error checking existing session: ${e.message}")
            tokenManager.clearTokens()
            _isLoggedIn.value = false
            _user.value = null
        }
    }

    /**
     * Attempt to refresh the access token using the stored refresh token.
     * Returns true if refresh succeeded, false otherwise.
     */
    private suspend fun tryRefreshToken(): Boolean {
        val refreshToken = tokenManager.getRefreshToken()
        if (refreshToken.isNullOrEmpty()) {
            Log.w("AuthViewModel", "No refresh token stored, cannot refresh")
            return false
        }
        return authService.refreshToken(refreshToken).fold(
            onSuccess = { response ->
                Log.d("AuthViewModel", "Token refresh succeeded")
                // Persist both the new access token AND the new refresh token (rotation)
                tokenManager.updateAccessToken(
                    accessToken = response.accessToken,
                    newRefreshToken = response.refreshToken
                )
                true
            },
            onFailure = { e ->
                Log.e("AuthViewModel", "Token refresh failed: ${e.message}")
                false
            }
        )
    }

    fun signInWithGoogle(idToken: String) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                _errorMessage.value = null

                // Call the backend API with the Google ID token
                authService.login(idToken).fold(
                    onSuccess = { loginResponse ->
                        // Save tokens and user info to TokenManager
                        Log.d("AuthViewModel", "Login successful: $loginResponse")
                        tokenManager.saveTokens(
                            accessToken = loginResponse.token,
                            refreshToken = loginResponse.refreshToken,
                        )

                        // Get user profile using getProfile API instead of using login response user
                        authService.getProfile().fold(
                            onSuccess = { user ->
                                Log.d("AuthViewModel", "Profile fetched after login: $user")

                                // Save user info to TokenManager
                                tokenManager.saveTokens(
                                    accessToken = loginResponse.token,
                                    refreshToken = loginResponse.refreshToken,
                                    userId = user.id,
                                    userName = user.name,
                                    userEmail = user.email,
                                    userPhotoUrl = user.avatar
                                )

                                _user.value = user
                                _isLoggedIn.value = true
                            },
                            onFailure = { profileException ->
                                Log.e("AuthViewModel", "Failed to get profile after login: ${profileException.message}")
                                // If profile fetch fails, still consider login successful but show error
                                _user.value = null
                                _isLoggedIn.value = true
                                _errorMessage.value = "Login successful but failed to load profile: ${profileException.message}"
                            }
                        )
                    },
                    onFailure = { exception ->
                        _errorMessage.value = "Sign-in failed: ${exception.message}"
                    }
                )

            } catch (e: Exception) {
                _errorMessage.value = "Sign-in failed: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun signOut() {
        viewModelScope.launch {
            try {
                // Call logout API if needed
                val refreshToken = tokenManager.getRefreshToken()
                refreshToken?.let { authService.logout(it) }

                // Clear stored tokens
                tokenManager.clearTokens()

                _user.value = null
                _isLoggedIn.value = false
                _errorMessage.value = null

            } catch (e: Exception) {
                // Even if logout API fails, clear local tokens
                tokenManager.clearTokens()
                _user.value = null
                _isLoggedIn.value = false
                _errorMessage.value = "Logout failed: ${e.message}"
            }
        }
    }

    /**
     * Refresh access token using refresh token
     */
    fun refreshToken() {
        viewModelScope.launch {
            val refreshed = tryRefreshToken()
            if (!refreshed) signOut()
        }
    }

    /**
     * Get authorization header for API calls
     */
    suspend fun getAuthorizationHeader(): String? {
        return tokenManager.getAuthorizationHeader()
    }

    /**
     * Check if token needs refresh and refresh if needed
     */
    suspend fun ensureValidToken(): Boolean {
        return when {
            !tokenManager.isTokenExpired() -> true
            tokenManager.hasRefreshToken() -> tryRefreshToken()
            else -> false
        }
    }

    fun setError(message: String) {
        _errorMessage.value = message
    }

    fun clearError() {
        _errorMessage.value = null
    }

    /**
     * Update the current user in the state (called after profile update)
     */
    fun updateUser(user: User) {
        _user.value = user
    }
}
