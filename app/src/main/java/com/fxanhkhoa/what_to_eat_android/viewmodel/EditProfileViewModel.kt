package com.fxanhkhoa.what_to_eat_android.viewmodel

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fxanhkhoa.what_to_eat_android.data.dto.User
import com.fxanhkhoa.what_to_eat_android.model.UpdateUserDto
import com.fxanhkhoa.what_to_eat_android.services.UserService
import com.fxanhkhoa.what_to_eat_android.utils.TokenManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class EditProfileUiState(
    val name: String = "",
    val email: String = "",
    val phone: String = "",
    val address: String = "",
    val dateOfBirth: String = "",
    val avatarUrl: String = "",
    val isLoading: Boolean = false,
    val isSaving: Boolean = false,
    val isSuccess: Boolean = false,
    val errorMessage: String? = null
)

class EditProfileViewModel(
    context: Context,
    private val authViewModel: AuthViewModel
) : ViewModel() {

    companion object {
        private const val TAG = "EditProfileViewModel"
    }

    private val appContext = context.applicationContext
    private val userService = UserService.getInstance(appContext)
    private val tokenManager = TokenManager.getInstance(appContext)

    private val _uiState = MutableStateFlow(EditProfileUiState())
    val uiState: StateFlow<EditProfileUiState> = _uiState.asStateFlow()

    init {
        loadCurrentUser()
    }

    private fun loadCurrentUser() {
        val user = authViewModel.user.value
        if (user != null) {
            _uiState.value = _uiState.value.copy(
                name = user.name,
                email = user.email,
                phone = "",
                address = "",
                dateOfBirth = user.dateOfBirth ?: "",
                avatarUrl = user.avatar ?: ""
            )
            // Also try to fetch full profile from backend for phone/address
            fetchFullProfile(user.id)
        }
    }

    private fun fetchFullProfile(userId: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            try {
                userService.findOne(userId).collect { result ->
                    result.fold(
                        onSuccess = { userModel ->
                            _uiState.value = _uiState.value.copy(
                                name = userModel.name ?: _uiState.value.name,
                                email = userModel.email,
                                phone = userModel.phone ?: "",
                                address = userModel.address ?: "",
                                dateOfBirth = userModel.dateOfBirth ?: "",
                                avatarUrl = userModel.avatar ?: "",
                                isLoading = false
                            )
                        },
                        onFailure = {
                            _uiState.value = _uiState.value.copy(isLoading = false)
                        }
                    )
                }
            } catch (e: Exception) {
                Log.e(TAG, "Failed to load full profile: ${e.message}", e)
                _uiState.value = _uiState.value.copy(isLoading = false)
            }
        }
    }

    fun onNameChange(value: String) {
        _uiState.value = _uiState.value.copy(name = value, errorMessage = null)
    }

    fun onPhoneChange(value: String) {
        _uiState.value = _uiState.value.copy(phone = value, errorMessage = null)
    }

    fun onAddressChange(value: String) {
        _uiState.value = _uiState.value.copy(address = value, errorMessage = null)
    }

    fun onDateOfBirthChange(value: String) {
        _uiState.value = _uiState.value.copy(dateOfBirth = value, errorMessage = null)
    }

    fun onAvatarUrlChange(value: String) {
        _uiState.value = _uiState.value.copy(avatarUrl = value, errorMessage = null)
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }

    fun clearSuccess() {
        _uiState.value = _uiState.value.copy(isSuccess = false)
    }

    fun saveProfile() {
        val userId = authViewModel.user.value?.id ?: run {
            _uiState.value = _uiState.value.copy(errorMessage = "User not authenticated")
            return
        }

        val state = _uiState.value

        if (state.name.isBlank()) {
            _uiState.value = _uiState.value.copy(errorMessage = "Name cannot be empty")
            return
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isSaving = true, errorMessage = null)
            try {
                val updateDto = UpdateUserDto(
                    id = userId,
                    email = state.email,
                    name = state.name.trim(),
                    phone = state.phone.trim().ifEmpty { null },
                    address = state.address.trim().ifEmpty { null },
                    dateOfBirth = state.dateOfBirth.ifEmpty { null }, // Convert to ISO string or null, // kept as string in UI; backend may need Date
                    avatar = state.avatarUrl.trim().ifEmpty { null }
                )

                Log.d(TAG, "updateDto: $updateDto")

                userService.update(updateDto).fold(
                    onSuccess = { updatedUser ->
                        Log.i(TAG, "Profile updated successfully")

                        // Refresh token manager user info
                        val accessToken = tokenManager.getAccessToken()
                        val refreshToken = tokenManager.getRefreshToken()
                        tokenManager.saveTokens(
                            accessToken = accessToken ?: "",
                            refreshToken = refreshToken,
                            userId = updatedUser.id,
                            userName = updatedUser.name,
                            userEmail = updatedUser.email,
                            userPhotoUrl = updatedUser.avatar
                        )

                        // Update auth view model user
                        val updatedDtoUser = User(
                            id = updatedUser.id,
                            name = updatedUser.name ?: state.name,
                            email = updatedUser.email,
                            avatar = updatedUser.avatar,
                            googleID = updatedUser.googleID,
                            roleName = updatedUser.roleName,
                            dateOfBirth = updatedUser.dateOfBirth,
                            deleted = updatedUser.deleted,
                            createdAt = updatedUser.createdAt,
                            updatedAt = updatedUser.updatedAt
                        )
                        authViewModel.updateUser(updatedDtoUser)

                        _uiState.value = _uiState.value.copy(
                            isSaving = false,
                            isSuccess = true
                        )
                    },
                    onFailure = { e ->
                        Log.e(TAG, "Failed to update profile: ${e.message}", e)
                        _uiState.value = _uiState.value.copy(
                            isSaving = false,
                            errorMessage = e.message ?: "Failed to update profile"
                        )
                    }
                )
            } catch (e: Exception) {
                Log.e(TAG, "Failed to update profile: ${e.message}", e)
                _uiState.value = _uiState.value.copy(
                    isSaving = false,
                    errorMessage = e.message ?: "Unexpected error occurred"
                )
            }
        }
    }
}



