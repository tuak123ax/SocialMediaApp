package com.minhtu.firesocialmedia.application.interactor

import com.minhtu.firesocialmedia.domain.entity.notification.NotificationInstance
import com.minhtu.firesocialmedia.domain.interactor.home.NotificationInteractor
import com.minhtu.firesocialmedia.domain.usecases.common.DeleteNotificationFromDatabaseUseCase
import com.minhtu.firesocialmedia.domain.usecases.common.SaveNotificationToDatabaseUseCase
import com.minhtu.firesocialmedia.domain.usecases.common.GetAllNotificationOfUserUseCase

class NotificationInteractorImpl(
    private val getAllNotificationOfUserUseCase: GetAllNotificationOfUserUseCase,
    private val saveNotificationToDatabaseUseCase: SaveNotificationToDatabaseUseCase,
    private val deleteNotificationFromDatabaseUseCase: DeleteNotificationFromDatabaseUseCase
) : NotificationInteractor {
    override suspend fun allNotificationsOf(path : String, userId: String): List<NotificationInstance>? {
        return getAllNotificationOfUserUseCase.invoke(
            path,
            userId
        )
    }

    override suspend fun saveNotificationToDatabase(
        id: String,
        path: String,
        instance: ArrayList<NotificationInstance>
    ) {
        saveNotificationToDatabaseUseCase.invoke(
            id,
            path,
            instance
        )
    }

    override suspend fun deleteNotificationFromDatabase(
        id: String,
        path: String,
        notification: NotificationInstance
    ) {
        deleteNotificationFromDatabaseUseCase.invoke(
            id,
            path,
            notification
        )
    }
}