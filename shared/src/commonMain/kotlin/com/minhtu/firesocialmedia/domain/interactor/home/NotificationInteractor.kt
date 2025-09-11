package com.minhtu.firesocialmedia.domain.interactor.home

import com.minhtu.firesocialmedia.domain.entity.notification.NotificationInstance

interface NotificationInteractor {
    suspend fun allNotificationsOf(path : String, userId: String): List<NotificationInstance>?
    suspend fun saveNotificationToDatabase(id : String,
                                           path : String,
                                           instance : ArrayList<NotificationInstance>)
    suspend fun deleteNotificationFromDatabase(id : String,
                                               path : String,
                                               notification: NotificationInstance)
}