package com.minhtu.firesocialmedia.domain.usecases.home

import com.minhtu.firesocialmedia.domain.entity.notification.NotificationInstance
import com.minhtu.firesocialmedia.domain.usecases.local.StoreNotificationsLocalUseCase

class StoreNotificationsToRoomUseCase(
    private val storeNotificationsLocalUseCase: StoreNotificationsLocalUseCase
) {
    suspend operator fun invoke(notifications: List<NotificationInstance>) {
        storeNotificationsLocalUseCase.invoke(notifications)
    }
}
