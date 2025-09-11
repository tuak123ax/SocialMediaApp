package com.minhtu.firesocialmedia.domain.repository

import com.minhtu.firesocialmedia.domain.entity.notification.NotificationInstance

interface NotificationRepository {
    suspend fun getAllNotificationsOfUser(path: String, currentUserUid: String) : List<NotificationInstance>?
    suspend fun saveNotificationToDatabase(id : String,
                                           path : String,
                                           instance : ArrayList<NotificationInstance>)
    suspend fun deleteNotificationFromDatabase(
        id: String,
        path: String,
        notification: NotificationInstance
    )
}