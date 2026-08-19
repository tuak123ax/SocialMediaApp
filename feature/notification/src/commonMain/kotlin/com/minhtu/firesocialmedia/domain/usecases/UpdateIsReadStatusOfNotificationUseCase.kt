package com.minhtu.firesocialmedia.domain.usecases.notification

import com.minhtu.firesocialmedia.domain.entity.notification.NotificationInstance
import com.minhtu.firesocialmedia.domain.repository.NotificationRepository

class UpdateIsReadStatusOfNotificationUseCase(
    private val notificationRepository: NotificationRepository
) {
    suspend operator fun invoke(userId : String,
                                notification: NotificationInstance) {
        notificationRepository.updateIsReadStatusOfNotification(userId, notification)
    }
}
