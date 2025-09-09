package com.fxanhkhoa.what_to_eat_android.utils

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import com.fxanhkhoa.what_to_eat_android.viewmodel.AuthViewModel

/**
 * Composable function to get the shared AuthViewModel instance
 * This ensures the same instance is used across the entire app
 */
@Composable
fun rememberSharedAuthViewModel(): AuthViewModel {
    val context = LocalContext.current
    val tokenManager = rememberTokenManager()
    return remember { AuthViewModel.getInstance(tokenManager) }
}
