package com.minhtu.firesocialmedia.domain.usecases.home

import com.minhtu.firesocialmedia.domain.entity.notification.NotificationInstance
import com.minhtu.firesocialmedia.domain.repository.LocalRepository

class StoreNotificationsToRoomUseCase(
    private val localRepository: LocalRepository
) {
    suspend operator fun invoke(notifications: List<NotificationInstance>) {
        localRepository.storeNotificationsToRoom(notifications)
    }
}