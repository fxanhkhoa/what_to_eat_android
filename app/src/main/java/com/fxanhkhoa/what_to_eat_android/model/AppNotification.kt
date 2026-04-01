package com.fxanhkhoa.what_to_eat_android.model

import java.util.UUID

/**
 * Lightweight notification event passed between FCMService and NotificationViewModel.
 */
data class AppNotification(
    val id: String = UUID.randomUUID().toString(),
    val title: String,
    val body: String,
    val type: String? = null,
    /** Navigation route to open when banner/notification is tapped */
    val deepLinkRoute: String? = null
)

