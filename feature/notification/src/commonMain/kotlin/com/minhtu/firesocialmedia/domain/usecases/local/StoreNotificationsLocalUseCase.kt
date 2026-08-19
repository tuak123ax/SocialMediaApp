package com.minhtu.firesocialmedia.domain.usecases.local

import com.minhtu.firesocialmedia.domain.entity.notification.NotificationInstance

interface StoreNotificationsLocalUseCase {
    suspend operator fun invoke(notifications: List<NotificationInstance>)
}
