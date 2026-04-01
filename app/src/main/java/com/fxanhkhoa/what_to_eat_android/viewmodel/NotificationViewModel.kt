package com.fxanhkhoa.what_to_eat_android.viewmodel

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fxanhkhoa.what_to_eat_android.data.dto.NotificationItem
import com.fxanhkhoa.what_to_eat_android.data.dto.NotificationPreferences
import com.fxanhkhoa.what_to_eat_android.data.dto.UpdateNotificationPreferencesDto
import com.fxanhkhoa.what_to_eat_android.model.AppNotification
import com.fxanhkhoa.what_to_eat_android.services.NotificationService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class NotificationViewModel private constructor(context: Context) : ViewModel() {

    companion object {
        private const val TAG = "NotificationViewModel"

        @Volatile
        private var INSTANCE: NotificationViewModel? = null

        fun getInstance(context: Context): NotificationViewModel =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: NotificationViewModel(context.applicationContext).also { INSTANCE = it }
            }

        /** Called by FCMService which has no ViewModelStoreOwner. May be null if app is killed. */
        fun getInstanceOrNull(): NotificationViewModel? = INSTANCE
    }

    // applicationContext is used only during init — not stored as a field
    private val notificationService = NotificationService.getInstance(context)

    // ── In-app banner state ──────────────────────────────────────────────────

    private val _inAppNotification = MutableStateFlow<AppNotification?>(null)
    val inAppNotification: StateFlow<AppNotification?> = _inAppNotification.asStateFlow()

    /** Deep-link route received via notification tap while app was in background */
    private val _pendingDeepLinkRoute = MutableStateFlow<String?>(null)
    val pendingDeepLinkRoute: StateFlow<String?> = _pendingDeepLinkRoute.asStateFlow()

    // ── History state ────────────────────────────────────────────────────────

    private val _notifications = MutableStateFlow<List<NotificationItem>>(emptyList())
    val notifications: StateFlow<List<NotificationItem>> = _notifications.asStateFlow()

    private val _unreadCount = MutableStateFlow(0)
    val unreadCount: StateFlow<Int> = _unreadCount.asStateFlow()

    // ── Preferences state ────────────────────────────────────────────────────

    private val _preferences = MutableStateFlow<NotificationPreferences?>(null)
    val preferences: StateFlow<NotificationPreferences?> = _preferences.asStateFlow()

    // ── Loading / error state ─────────────────────────────────────────────────

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    // ── In-app banner ────────────────────────────────────────────────────────

    /** Called by FCMService when a message arrives while the app is in the foreground. */
    fun postInAppNotification(notification: AppNotification) {
        _inAppNotification.value = notification
    }

    fun dismissInAppNotification() {
        _inAppNotification.value = null
    }

    // ── Pending deep-link (from system notification tap) ─────────────────────

    fun setPendingDeepLinkRoute(route: String?) {
        _pendingDeepLinkRoute.value = route
    }

    /** Consume the pending route (returns it and clears the state). */
    fun consumePendingDeepLinkRoute(): String? {
        val route = _pendingDeepLinkRoute.value
        _pendingDeepLinkRoute.value = null
        return route
    }

    // ── History ───────────────────────────────────────────────────────────────

    fun loadNotifications() {
        viewModelScope.launch {
            _isLoading.value = true
            notificationService.getNotifications().fold(
                onSuccess = { list ->
                    _notifications.value = list
                    _unreadCount.value = list.count { !it.isRead }
                },
                onFailure = { e ->
                    Log.e(TAG, "loadNotifications error: ${e.message}")
                    _errorMessage.value = e.message
                }
            )
            _isLoading.value = false
        }
    }

    fun refreshUnreadCount() {
        viewModelScope.launch {
            notificationService.getUnreadCount().fold(
                onSuccess = { count -> _unreadCount.value = count },
                onFailure = { e -> Log.e(TAG, "refreshUnreadCount error: ${e.message}") }
            )
        }
    }

    fun markAsRead(id: String) {
        viewModelScope.launch {
            notificationService.markAsRead(id).fold(
                onSuccess = {
                    val now = java.time.Instant.now().toString()
                    _notifications.value = _notifications.value.map { item ->
                        if (item.id == id) item.copy(readAt = now) else item
                    }
                    _unreadCount.value = (_unreadCount.value - 1).coerceAtLeast(0)
                },
                onFailure = { e -> Log.e(TAG, "markAsRead error: ${e.message}") }
            )
        }
    }

    fun markAllAsRead() {
        viewModelScope.launch {
            notificationService.markAllAsRead().fold(
                onSuccess = {
                    val now = java.time.Instant.now().toString()
                    _notifications.value = _notifications.value.map { it.copy(readAt = now) }
                    _unreadCount.value = 0
                },
                onFailure = { e -> Log.e(TAG, "markAllAsRead error: ${e.message}") }
            )
        }
    }

    // ── Preferences ───────────────────────────────────────────────────────────

    fun loadPreferences() {
        viewModelScope.launch {
            notificationService.getPreferences().fold(
                onSuccess = { prefs -> _preferences.value = prefs },
                onFailure = { e -> Log.e(TAG, "loadPreferences error: ${e.message}") }
            )
        }
    }

    fun updatePreferences(dto: UpdateNotificationPreferencesDto) {
        viewModelScope.launch {
            notificationService.updatePreferences(dto).fold(
                onSuccess = { updated -> _preferences.value = updated },
                onFailure = { e ->
                    Log.e(TAG, "updatePreferences error: ${e.message}")
                    _errorMessage.value = e.message
                }
            )
        }
    }

    fun clearError() {
        _errorMessage.value = null
    }
}


