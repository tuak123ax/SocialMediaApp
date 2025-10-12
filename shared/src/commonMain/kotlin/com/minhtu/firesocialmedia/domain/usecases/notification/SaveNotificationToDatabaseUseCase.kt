package com.minhtu.firesocialmedia.domain.usecases.notification

import com.minhtu.firesocialmedia.domain.entity.notification.NotificationInstance
import com.minhtu.firesocialmedia.domain.repository.NotificationRepository

class SaveNotificationToDatabaseUseCase(
    private val notificationRepository: NotificationRepository
) {
    suspend operator fun invoke(id : String,
                                instance : ArrayList<NotificationInstance>) {
        notificationRepository.saveNotificationToDatabase(id, instance)
    }
}