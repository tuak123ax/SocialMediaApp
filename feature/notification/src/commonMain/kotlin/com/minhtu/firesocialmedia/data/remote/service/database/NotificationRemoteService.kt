package com.minhtu.firesocialmedia.data.remote.service.database

import com.minhtu.firesocialmedia.notification.data.remote.dto.notification.NotificationDTO

/**
 * Owns the remote (Firebase) persistence for a user's notification list.
 * Moved out of core's generic `DatabaseService` — notifications are a
 * feature:notification concern, not something the infra module should know about.
 */
interface NotificationRemoteService {
    suspend fun getAllNotificationsOfUser(
        path: String,
        currentUserUid: String
    ): List<NotificationDTO>?

    suspend fun saveNotificationToDatabase(
        id: String,
        path: String,
        instance: ArrayList<NotificationDTO>
    )

    suspend fun deleteNotificationFromDatabase(
        id: String,
        path: String,
        notification: NotificationDTO
    )

    suspend fun updateIsReadStatusOfNotification(
        userId: String,
        notificationId: String,
        userPath: String,
        notificationPath: String
    )

    suspend fun deleteAllNotifications(
        uid: String,
        userPath: String,
        notificationPath: String
    ): Result<Unit>
}
