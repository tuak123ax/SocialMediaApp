package com.minhtu.firesocialmedia.core.domain.usecases.notification

import com.minhtu.firesocialmedia.core.domain.entity.notification.NotificationInstance
import com.minhtu.firesocialmedia.core.domain.repository.NotificationRepository

class DeleteNotificationFromDatabaseUseCase(
    private val notificationRepository: NotificationRepository
) {
    suspend operator fun invoke(id : String,
                                instance : NotificationInstance) {
        notificationRepository.deleteNotificationFromDatabase(id,instance)
    }
}