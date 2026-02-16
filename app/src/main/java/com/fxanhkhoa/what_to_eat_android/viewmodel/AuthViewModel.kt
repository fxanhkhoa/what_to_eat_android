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
            if (tokenManager.hasValidTokens()) {
                Log.d("AuthViewModel", "Valid tokens found, fetching profile")
                // Call getProfile() instead of creating User directly
                authService.getProfile().fold(
                    onSuccess = { user ->
                        Log.d("AuthViewModel", "Profile fetched successfully: $user")

                        // Save user info to TokenManager if not already present
                        val currentUserInfo = tokenManager.getUserInfo()
                        if (currentUserInfo?.id == null) {
                            val accessToken = tokenManager.getAccessToken()
                            val refreshToken = tokenManager.getRefreshToken()
                            tokenManager.saveTokens(
                                accessToken = accessToken ?: "",
                                refreshToken = refreshToken,
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
                        // If profile fetch fails, clear tokens and reset state
                        tokenManager.clearTokens()
                        _isLoggedIn.value = false
                        _user.value = null
                    }
                )
            }
        } catch (e: Exception) {
            Log.e("AuthViewModel", "Error checking existing session: ${e.message}")
            // If there's an error, clear tokens and reset state
            tokenManager.clearTokens()
            _isLoggedIn.value = false
            _user.value = null
        }
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
            try {
                val refreshToken = tokenManager.getRefreshToken()
                if (refreshToken != null) {
                    authService.refreshToken(refreshToken).fold(
                        onSuccess = { refreshResponse ->
                            // Update access token
                            tokenManager.updateAccessToken(
                                accessToken = refreshResponse.accessToken,
                            )
                        },
                        onFailure = {
                            // If refresh fails, sign out user
                            signOut()
                        }
                    )
                } else {
                    // No refresh token available, sign out
                    signOut()
                }
            } catch (e: Exception) {
                // If refresh fails, sign out user
                signOut()
            }
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
        return if (tokenManager.isTokenExpired() && tokenManager.hasRefreshToken()) {
            refreshToken()
            tokenManager.hasValidTokens()
        } else {
            tokenManager.hasValidTokens()
        }
    }

    fun setError(message: String) {
        _errorMessage.value = message
    }

    fun clearError() {
        _errorMessage.value = null
    }
}
