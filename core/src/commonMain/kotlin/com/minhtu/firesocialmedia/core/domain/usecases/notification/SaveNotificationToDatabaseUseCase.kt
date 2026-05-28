package com.minhtu.firesocialmedia.core.domain.usecases.notification

import com.minhtu.firesocialmedia.core.domain.entity.notification.NotificationInstance
import com.minhtu.firesocialmedia.core.domain.repository.NotificationRepository

class SaveNotificationToDatabaseUseCase(
    private val notificationRepository: NotificationRepository
) {
    suspend operator fun invoke(id : String,
                                instance : ArrayList<NotificationInstance>) {
        notificationRepository.saveNotificationToDatabase(id, instance)
    }
}