package com.minhtu.firesocialmedia.domain.usecases.notification

import com.minhtu.firesocialmedia.domain.repository.NotificationRepository

class DeleteAllNotificationsUseCase(
    private val notificationRepository: NotificationRepository
) {
    suspend operator fun invoke(userId : String) : Result<Unit> {
        return notificationRepository.deleteAllNotifications(userId)
    }
}
