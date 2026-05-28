package com.minhtu.firesocialmedia.core.domain.usecases.notification

import com.minhtu.firesocialmedia.core.domain.entity.user.UserInstance
import com.minhtu.firesocialmedia.core.domain.repository.NotificationRepository

class DeleteAllNotificationsUseCase(
    private val notificationRepository: NotificationRepository
) {
    suspend operator fun invoke(user : UserInstance) : Result<Unit> {
        return notificationRepository.deleteAllNotifications(user)
    }
}