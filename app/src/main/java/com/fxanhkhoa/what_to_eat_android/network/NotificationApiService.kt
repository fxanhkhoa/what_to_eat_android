package com.fxanhkhoa.what_to_eat_android.network

import com.fxanhkhoa.what_to_eat_android.data.dto.*
import com.fxanhkhoa.what_to_eat_android.model.APIPagination
import retrofit2.Response
import retrofit2.http.*

interface NotificationApiService {

    /** Register FCM device token for the current user */
    @POST("notification/register-token/")
    suspend fun registerToken(@Body dto: RegisterTokenDto): Response<Unit>

    /** Unregister FCM device token */
    @POST("notification/unregister-token/")
    suspend fun unregisterToken(@Body dto: UnregisterTokenDto): Response<Unit>

    /** Fetch notification history for the current user */
    @GET("notification/")
    suspend fun getNotifications(): Response<APIPagination<NotificationItem>>

    /** Get unread notification count */
    @GET("notification/unread-count/")
    suspend fun getUnreadCount(): Response<UnreadCountResponse>

    /** Mark a single notification as read */
    @PUT("notification/{id}/read/")
    suspend fun markAsRead(@Path("id") id: String): Response<Unit>

    /** Mark all notifications as read */
    @PUT("notification/read-all/")
    suspend fun markAllAsRead(): Response<Unit>

    /** Get notification preferences */
    @GET("notification/preferences/")
    suspend fun getPreferences(): Response<NotificationPreferences>

    /** Update notification preferences */
    @PUT("notification/preferences/")
    suspend fun updatePreferences(@Body dto: UpdateNotificationPreferencesDto): Response<NotificationPreferences>

    /** Admin: send a push notification manually */
    @POST("notification/send/")
    suspend fun send(@Body dto: SendNotificationDto): Response<Unit>
}

