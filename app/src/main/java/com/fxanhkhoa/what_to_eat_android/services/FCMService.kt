package com.fxanhkhoa.what_to_eat_android.services

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import android.util.Log
import androidx.core.app.NotificationCompat
import com.fxanhkhoa.what_to_eat_android.MainActivity
import com.fxanhkhoa.what_to_eat_android.R
import com.fxanhkhoa.what_to_eat_android.model.AppNotification
import com.fxanhkhoa.what_to_eat_android.network.RetrofitProvider
import com.fxanhkhoa.what_to_eat_android.utils.AppState
import com.fxanhkhoa.what_to_eat_android.utils.TokenManager
import com.fxanhkhoa.what_to_eat_android.viewmodel.NotificationViewModel
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class FCMService : FirebaseMessagingService() {

    companion object {
        const val CHANNEL_ID = "what_to_eat_notifications"
        const val CHANNEL_NAME = "Foodiary Notifications"
        private const val TAG = "FCMService"
        const val EXTRA_DEEP_LINK = "deepLinkRoute"
    }

    // ── Incoming message ─────────────────────────────────────────────────────

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        Log.d(TAG, "Message from: ${remoteMessage.from}")

        // Extract payload – prefer data fields for full control
        val title = remoteMessage.data["title"]
            ?: remoteMessage.notification?.title
            ?: "Foodiary"
        val body = remoteMessage.data["body"]
            ?: remoteMessage.notification?.body
            ?: ""
        val type = remoteMessage.data["type"]
        val deepLinkRoute = remoteMessage.data["deepLinkRoute"]

        val notification = AppNotification(
            title = title,
            body = body,
            type = type,
            deepLinkRoute = deepLinkRoute
        )

        if (AppState.isInForeground) {
            // ── In-app banner (app is open) ──────────────────────────────────
            NotificationViewModel.getInstanceOrNull()?.postInAppNotification(notification)
        } else {
            // ── System tray notification (app is in background / killed) ─────
            showSystemNotification(title, body, deepLinkRoute)
        }
    }

    // ── Token refresh ─────────────────────────────────────────────────────────

    override fun onNewToken(token: String) {
        Log.d(TAG, "FCM token refreshed")
        CoroutineScope(Dispatchers.IO).launch {
            val tokenManager = TokenManager.getInstance(applicationContext)
            tokenManager.saveFCMToken(token)

            // Register with backend only when the user is logged in
            if (tokenManager.hasAccessToken()) {
                // Ensure RetrofitProvider is initialised (may not be if the app is not running)
                RetrofitProvider.initialize(applicationContext)
                NotificationService.getInstance(applicationContext)
                    .registerToken(token)
                    .onFailure { e -> Log.e(TAG, "Failed to register new FCM token: ${e.message}") }
            }
        }
    }

    // ── System notification builder ───────────────────────────────────────────

    private fun showSystemNotification(title: String, body: String, deepLinkRoute: String?) {
        ensureChannelExists()

        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            deepLinkRoute?.let { putExtra(EXTRA_DEEP_LINK, it) }
        }

        val pendingIntent = PendingIntent.getActivity(
            this,
            System.currentTimeMillis().toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle(title)
            .setContentText(body)
            .setStyle(NotificationCompat.BigTextStyle().bigText(body))
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .build()

        val manager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        manager.notify(System.currentTimeMillis().toInt(), notification)
        Log.d(TAG, "System notification shown: $title")
    }

    private fun ensureChannelExists() {
        val manager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        if (manager.getNotificationChannel(CHANNEL_ID) == null) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Foodiary push notifications"
                enableVibration(true)
            }
            manager.createNotificationChannel(channel)
        }
    }
}


