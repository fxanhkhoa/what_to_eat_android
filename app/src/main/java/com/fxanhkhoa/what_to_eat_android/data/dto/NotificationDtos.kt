package com.fxanhkhoa.what_to_eat_android.data.dto

import com.google.gson.annotations.SerializedName

// ── Enums ────────────────────────────────────────────────────────────────────

object NotificationType {
    const val CHAT = "chat"
    const val ACTIVITY = "activity"
    const val MARKETING = "marketing"
}

// ── Request DTOs ─────────────────────────────────────────────────────────────

data class RegisterTokenDto(
    val token: String,
    val platform: String = "android",       // "web" | "android"
    val deviceInfo: String? = null
)

data class UnregisterTokenDto(
    val token: String
)

/**
 * All fields optional — only send what the user actually changed.
 * Matches UpdateNotificationPreferenceDto on the backend.
 */
data class UpdateNotificationPreferencesDto(
    val chatEnabled: Boolean? = null,
    val activityEnabled: Boolean? = null,
    val marketingEnabled: Boolean? = null,
    val quietHoursStart: String? = null,    // e.g. "22:00"
    val quietHoursEnd: String? = null       // e.g. "08:00"
)

data class SendNotificationDto(
    val userId: String,
    val title: String,
    val body: String,
    val imageUrl: String? = null,
    val data: Map<String, String>? = null,
    val type: String                        // "chat" | "activity" | "marketing"
)

// ── Response DTOs ─────────────────────────────────────────────────────────────

data class NotificationItem(
    @SerializedName("_id") val id: String,
    val userId: String = "",
    val title: String,
    val body: String,
    val imageUrl: String? = null,
    val data: Map<String, String>? = null,
    val type: String? = null,               // "chat" | "activity" | "marketing"
    val deepLinkRoute: String? = null,      // derived from data["deepLinkRoute"] if present
    /** Non-null means the notification has been read */
    val readAt: String? = null,
    val sentAt: String? = null,
    val clickedAt: String? = null,
    val sendError: String? = null,
    val createdAt: String? = null,
    val updatedAt: String? = null
) {
    /** Convenience property mirroring the backend ReadAt-pointer semantics */
    val isRead: Boolean get() = readAt != null
}

data class UnreadCountResponse(
    val count: Int
)

/**
 * Matches NotificationPreference struct on the backend.
 */
data class NotificationPreferences(
    @SerializedName("_id") val id: String? = null,
    val userId: String? = null,
    val chatEnabled: Boolean = true,
    val activityEnabled: Boolean = true,
    val marketingEnabled: Boolean = true,
    val quietHoursStart: String? = null,    // e.g. "22:00"
    val quietHoursEnd: String? = null       // e.g. "08:00"
)
