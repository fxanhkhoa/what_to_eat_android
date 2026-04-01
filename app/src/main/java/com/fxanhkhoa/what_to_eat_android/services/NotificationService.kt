package com.fxanhkhoa.what_to_eat_android.services

import android.content.Context
import android.util.Log
import com.fxanhkhoa.what_to_eat_android.data.dto.*
import com.fxanhkhoa.what_to_eat_android.network.RetrofitProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class NotificationService private constructor(context: Context) {

    companion object {
        private const val TAG = "NotificationService"

        @Volatile
        private var INSTANCE: NotificationService? = null

        fun getInstance(context: Context): NotificationService =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: NotificationService(context.applicationContext).also { INSTANCE = it }
            }
    }

    // Always fetches the current Retrofit instance (picks up AuthInterceptor after initialize())
    private val api get() = RetrofitProvider.notificationService

    // ── Token management ────────────────────────────────────────────────────

    suspend fun registerToken(token: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val res = api.registerToken(RegisterTokenDto(token = token))
            if (res.isSuccessful) {
                Log.i(TAG, "FCM token registered")
                Result.success(Unit)
            } else {
                Result.failure(Exception("registerToken failed: ${res.message()}"))
            }
        } catch (e: Exception) {
            Log.e(TAG, "registerToken error: ${e.message}", e)
            Result.failure(e)
        }
    }

    suspend fun unregisterToken(token: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val res = api.unregisterToken(UnregisterTokenDto(token = token))
            if (res.isSuccessful) {
                Log.i(TAG, "FCM token unregistered")
                Result.success(Unit)
            } else {
                Result.failure(Exception("unregisterToken failed: ${res.message()}"))
            }
        } catch (e: Exception) {
            Log.e(TAG, "unregisterToken error: ${e.message}", e)
            Result.failure(e)
        }
    }

    // ── Notification history ────────────────────────────────────────────────

    suspend fun getNotifications(): Result<List<NotificationItem>> = withContext(Dispatchers.IO) {
        try {
            val res = api.getNotifications()
            if (res.isSuccessful && res.body() != null) {
                Result.success(res.body()!!.data)
            } else {
                Result.failure(Exception("getNotifications failed: ${res.message()}"))
            }
        } catch (e: Exception) {
            Log.e(TAG, "getNotifications error: ${e.message}", e)
            Result.failure(e)
        }
    }

    suspend fun getUnreadCount(): Result<Int> = withContext(Dispatchers.IO) {
        try {
            val res = api.getUnreadCount()
            if (res.isSuccessful && res.body() != null) {
                Result.success(res.body()!!.count)
            } else {
                Result.failure(Exception("getUnreadCount failed: ${res.message()}"))
            }
        } catch (e: Exception) {
            Log.e(TAG, "getUnreadCount error: ${e.message}", e)
            Result.failure(e)
        }
    }

    suspend fun markAsRead(id: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val res = api.markAsRead(id)
            if (res.isSuccessful) Result.success(Unit)
            else Result.failure(Exception("markAsRead failed: ${res.message()}"))
        } catch (e: Exception) {
            Log.e(TAG, "markAsRead error: ${e.message}", e)
            Result.failure(e)
        }
    }

    suspend fun markAllAsRead(): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val res = api.markAllAsRead()
            if (res.isSuccessful) Result.success(Unit)
            else Result.failure(Exception("markAllAsRead failed: ${res.message()}"))
        } catch (e: Exception) {
            Log.e(TAG, "markAllAsRead error: ${e.message}", e)
            Result.failure(e)
        }
    }

    // ── Preferences ─────────────────────────────────────────────────────────

    suspend fun getPreferences(): Result<NotificationPreferences> = withContext(Dispatchers.IO) {
        try {
            val res = api.getPreferences()
            if (res.isSuccessful && res.body() != null) {
                Result.success(res.body()!!)
            } else {
                Result.failure(Exception("getPreferences failed: ${res.message()}"))
            }
        } catch (e: Exception) {
            Log.e(TAG, "getPreferences error: ${e.message}", e)
            Result.failure(e)
        }
    }

    suspend fun updatePreferences(dto: UpdateNotificationPreferencesDto): Result<NotificationPreferences> =
        withContext(Dispatchers.IO) {
            try {
                val res = api.updatePreferences(dto)
                if (res.isSuccessful && res.body() != null) {
                    Result.success(res.body()!!)
                } else {
                    Result.failure(Exception("updatePreferences failed: ${res.message()}"))
                }
            } catch (e: Exception) {
                Log.e(TAG, "updatePreferences error: ${e.message}", e)
                Result.failure(e)
            }
        }

    // ── Admin ────────────────────────────────────────────────────────────────

    suspend fun send(dto: SendNotificationDto): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val res = api.send(dto)
            if (res.isSuccessful) Result.success(Unit)
            else Result.failure(Exception("send failed: ${res.message()}"))
        } catch (e: Exception) {
            Log.e(TAG, "send error: ${e.message}", e)
            Result.failure(e)
        }
    }
}


