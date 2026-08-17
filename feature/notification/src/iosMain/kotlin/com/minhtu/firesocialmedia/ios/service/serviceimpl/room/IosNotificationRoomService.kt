package com.minhtu.firesocialmedia.ios.service.serviceimpl.room

import com.minhtu.firesocialmedia.domain.entity.notification.NotificationInstance
import com.minhtu.firesocialmedia.data.local.service.room.NotificationRoomService

/**
 * No-op stub, matching the original (pre-Phase-3) `IosRoomService` convention: iOS Room access is
 * not wired up to a real database (see instruction.md).
 */
class IosNotificationRoomService : NotificationRoomService {
    override suspend fun storeNotificationsToRoom(notifications: List<NotificationInstance>) {
        // no-op
    }

    override suspend fun getAllNotifications(): List<NotificationInstance> {
        return emptyList()
    }
}
