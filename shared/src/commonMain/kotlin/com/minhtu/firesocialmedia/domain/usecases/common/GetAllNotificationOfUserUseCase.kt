package com.minhtu.firesocialmedia.domain.usecases.common

import com.minhtu.firesocialmedia.domain.entity.notification.NotificationInstance
import com.minhtu.firesocialmedia.domain.repository.NotificationRepository

class GetAllNotificationOfUserUseCase(
    private val notificationRepository: NotificationRepository
) {
    suspend operator fun invoke(path: String, currentUserUid: String) : List<NotificationInstance>? {
        return notificationRepository.getAllNotificationsOfUser(path, currentUserUid)
    }
}