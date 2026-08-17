package com.minhtu.firesocialmedia.presentation.notification

import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.snapshots.SnapshotStateList
import com.minhtu.firesocialmedia.domain.entity.notification.NotificationInstance
import com.minhtu.firesocialmedia.domain.entity.user.notification.UserInstance
import com.minhtu.firesocialmedia.domain.interactor.home.NotificationInteractor
import com.minhtu.firesocialmedia.domain.usecases.common.notification.GetCurrentUserUidUseCase
import com.minhtu.firesocialmedia.platform.logMessage
import com.rickclephas.kmp.observableviewmodel.ViewModel
import com.rickclephas.kmp.observableviewmodel.launch
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class NotificationsViewModel(
    private val getCurrentUserUidUseCase: GetCurrentUserUidUseCase,
    private val notificationInteractor: NotificationInteractor,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) : ViewModel() {
    var listNotificationOfCurrentUser: SnapshotStateList<NotificationInstance> = mutableStateListOf()

    private val _allNotifications = MutableStateFlow<List<NotificationInstance>>(emptyList())
    val allNotifications = _allNotifications.asStateFlow()

    private val _getAllNotificationsOfCurrentUser = mutableStateOf(false)
    val getAllNotificationsOfCurrentUser = _getAllNotificationsOfCurrentUser

    fun getAllNotificationsOfUser() {
        viewModelScope.launch(ioDispatcher) {
            val currentUserId = getCurrentUserUidUseCase.invoke()
            if (currentUserId != null) {
                val notifications = notificationInteractor.allNotificationsOf(currentUserId)
                if (notifications != null) {
                    for (notification in notifications) {
                        logMessage("getAllNotifications",
                            { notification.id + "isRead: " + notification.beRead })
                    }
                    listNotificationOfCurrentUser.clear()
                    listNotificationOfCurrentUser.addAll(notifications)
                    updateNotifications(ArrayList(listNotificationOfCurrentUser.toList()))
                    _getAllNotificationsOfCurrentUser.value = true
                } else {
                    _getAllNotificationsOfCurrentUser.value = false
                }
            }
        }
    }

    fun removeNotificationInList(notification: NotificationInstance) {
        listNotificationOfCurrentUser.remove(notification)
    }

    fun updateNotifications(notifications: ArrayList<NotificationInstance>) {
        _allNotifications.value = notifications
    }

    suspend fun deleteNotification(notification: NotificationInstance, currentUser: UserInstance) {
        currentUser.notifications.remove(notification)
        notificationInteractor.deleteNotificationFromDatabase(
            currentUser.uid,
            notification
        )
    }
}
