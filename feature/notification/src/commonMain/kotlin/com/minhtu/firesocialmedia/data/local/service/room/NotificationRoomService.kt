package com.minhtu.firesocialmedia.data.local.service.room

import com.minhtu.firesocialmedia.domain.entity.notification.NotificationInstance

interface NotificationRoomService {
    suspend fun storeNotificationsToRoom(notifications: List<NotificationInstance>)
    suspend fun getAllNotifications(): List<NotificationInstance>
}
