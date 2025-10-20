package com.minhtu.firesocialmedia.application.interactor

import com.minhtu.firesocialmedia.domain.entity.notification.NotificationInstance
import com.minhtu.firesocialmedia.domain.interactor.home.NotificationInteractor
import com.minhtu.firesocialmedia.domain.usecases.home.StoreNotificationsToRoomUseCase
import com.minhtu.firesocialmedia.domain.usecases.notification.DeleteNotificationFromDatabaseUseCase
import com.minhtu.firesocialmedia.domain.usecases.notification.GetAllNotificationOfUserUseCase
import com.minhtu.firesocialmedia.domain.usecases.notification.SaveNotificationToDatabaseUseCase

class NotificationInteractorImpl(
    private val getAllNotificationOfUserUseCase: GetAllNotificationOfUserUseCase,
    private val saveNotificationToDatabaseUseCase: SaveNotificationToDatabaseUseCase,
    private val deleteNotificationFromDatabaseUseCase: DeleteNotificationFromDatabaseUseCase,
    private val storeNotificationsToRoomUseCase: StoreNotificationsToRoomUseCase
) : NotificationInteractor {
    override suspend fun allNotificationsOf(userId: String): List<NotificationInstance>? {
        return getAllNotificationOfUserUseCase.invoke(
            userId
        )
    }

    override suspend fun saveNotificationToDatabase(
        id: String,
        instance: ArrayList<NotificationInstance>
    ) {
        saveNotificationToDatabaseUseCase.invoke(
            id,
            instance
        )
    }

    override suspend fun deleteNotificationFromDatabase(
        id: String,
        notification: NotificationInstance
    ) {
        deleteNotificationFromDatabaseUseCase.invoke(
            id,
            notification
        )
    }

    override suspend fun storeNotificationsToRoom(notifications: List<NotificationInstance>) {
        storeNotificationsToRoomUseCase.invoke(notifications)
    }
}