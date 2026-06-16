package com.minhtu.firesocialmedia.feature.notification.presentation.setting.notificationconfigs

import androidx.lifecycle.ViewModel
import com.minhtu.firesocialmedia.core.domain.entity.notification.NotificationType
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class NotificationConfigsViewModel(
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) : ViewModel() {
    private val _notificationSettings =
        MutableStateFlow(
            mapOf(
                NotificationType.LIKE to true,
                NotificationType.COMMENT to true,
                NotificationType.ADD_FRIEND to true,
                NotificationType.UPLOAD_NEW to true,
                NotificationType.SHARE_NEW to true,
                NotificationType.INVITE_TO_GROUP to true
            )
        )

    val notificationSettings = _notificationSettings.asStateFlow()

    fun updateNotification(type: NotificationType, enabled: Boolean) {
        _notificationSettings.update { current ->
            current.toMutableMap().apply {
                this[type] = enabled
            }
        }
    }
}