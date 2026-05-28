package com.minhtu.firesocialmedia.core.domain.usecases.notification

import com.minhtu.firesocialmedia.core.domain.entity.notification.NotificationInstance
import com.minhtu.firesocialmedia.core.domain.entity.user.UserInstance
import com.minhtu.firesocialmedia.core.domain.repository.NotificationRepository

class UpdateIsReadStatusOfNotificationUseCase(
    private val notificationRepository: NotificationRepository
) {
    suspend operator fun invoke(user : UserInstance,
                                notification: NotificationInstance) {
        notificationRepository.updateIsReadStatusOfNotification(user, notification)
    }
}