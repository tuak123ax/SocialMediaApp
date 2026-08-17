package com.minhtu.firesocialmedia.data.repository.local

import com.minhtu.firesocialmedia.domain.entity.notification.NotificationInstance
import com.minhtu.firesocialmedia.data.local.service.room.NotificationRoomService
import com.minhtu.firesocialmedia.domain.usecases.local.StoreNotificationsLocalUseCase

class StoreNotificationsLocalUseCaseImpl(
    private val notificationRoomService: NotificationRoomService
) : StoreNotificationsLocalUseCase {
    override suspend fun invoke(notifications: List<NotificationInstance>) {
        notificationRoomService.storeNotificationsToRoom(notifications)
    }
}
