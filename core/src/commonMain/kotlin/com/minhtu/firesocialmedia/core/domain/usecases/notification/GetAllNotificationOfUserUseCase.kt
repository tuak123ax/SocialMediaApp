package com.minhtu.firesocialmedia.core.domain.usecases.notification

import com.minhtu.firesocialmedia.core.domain.entity.notification.NotificationInstance
import com.minhtu.firesocialmedia.core.domain.repository.NotificationRepository

class GetAllNotificationOfUserUseCase(
    private val notificationRepository: NotificationRepository
) {
    suspend operator fun invoke(currentUserUid: String) : List<NotificationInstance>? {
        return notificationRepository.getAllNotificationsOfUser(currentUserUid)
    }
}