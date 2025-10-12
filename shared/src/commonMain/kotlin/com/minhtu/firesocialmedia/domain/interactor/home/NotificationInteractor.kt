package com.minhtu.firesocialmedia.domain.interactor.home

import com.minhtu.firesocialmedia.domain.entity.notification.NotificationInstance

interface NotificationInteractor {
    suspend fun allNotificationsOf(userId: String): List<NotificationInstance>?
    suspend fun saveNotificationToDatabase(id : String,
                                           instance : ArrayList<NotificationInstance>)
    suspend fun deleteNotificationFromDatabase(id : String,
                                               notification: NotificationInstance)
    suspend fun storeNotificationsToRoom(notifications : List<NotificationInstance>)
}