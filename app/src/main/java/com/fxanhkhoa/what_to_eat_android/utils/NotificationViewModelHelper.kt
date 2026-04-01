package com.fxanhkhoa.what_to_eat_android.utils

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import com.fxanhkhoa.what_to_eat_android.viewmodel.NotificationViewModel

@Composable
fun rememberSharedNotificationViewModel(): NotificationViewModel {
    val context = LocalContext.current
    return remember { NotificationViewModel.getInstance(context) }
}

