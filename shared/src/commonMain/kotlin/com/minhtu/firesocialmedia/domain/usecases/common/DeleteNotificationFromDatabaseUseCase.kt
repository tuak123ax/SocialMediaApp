package com.minhtu.firesocialmedia.domain.usecases.common

import com.minhtu.firesocialmedia.domain.entity.notification.NotificationInstance
import com.minhtu.firesocialmedia.domain.repository.NotificationRepository

class DeleteNotificationFromDatabaseUseCase(
    private val notificationRepository: NotificationRepository
) {
    suspend operator fun invoke(id : String,
                                path : String,
                                instance : NotificationInstance) {
        notificationRepository.deleteNotificationFromDatabase(id,path,instance)
    }
}