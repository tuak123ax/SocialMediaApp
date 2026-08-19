package com.minhtu.firesocialmedia.android.service.serviceimpl.room

import com.minhtu.firesocialmedia.domain.entity.notification.NotificationInstance
import com.minhtu.firesocialmedia.data.local.dao.NotificationDao
import com.minhtu.firesocialmedia.data.local.mapper.room.toNotificationEntity
import com.minhtu.firesocialmedia.data.local.mapper.room.toNotificationInstance
import com.minhtu.firesocialmedia.data.local.service.room.NotificationRoomService

class AndroidNotificationRoomService(
    private val notificationDao: NotificationDao
) : NotificationRoomService {
    override suspend fun storeNotificationsToRoom(notifications: List<NotificationInstance>) {
        if (notifications.isNotEmpty()) {
            notificationDao.addAll(notifications.toNotificationEntity())
        }
    }

    override suspend fun getAllNotifications(): List<NotificationInstance> {
        return notificationDao.getAll().toNotificationInstance()
    }
}
