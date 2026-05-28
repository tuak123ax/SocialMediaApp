package com.minhtu.firesocialmedia.core.domain.usecases.home

import com.minhtu.firesocialmedia.core.domain.entity.notification.NotificationInstance
import com.minhtu.firesocialmedia.core.domain.repository.LocalRepository

class StoreNotificationsToRoomUseCase(
    private val localRepository: LocalRepository
) {
    suspend operator fun invoke(notifications: List<NotificationInstance>) {
        localRepository.storeNotificationsToRoom(notifications)
    }
}