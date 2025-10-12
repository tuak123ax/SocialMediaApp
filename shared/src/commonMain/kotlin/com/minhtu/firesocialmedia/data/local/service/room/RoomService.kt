package com.minhtu.firesocialmedia.data.local.service.room

import com.minhtu.firesocialmedia.domain.entity.news.NewsInstance
import com.minhtu.firesocialmedia.domain.entity.notification.NotificationInstance
import com.minhtu.firesocialmedia.domain.entity.user.UserInstance

interface RoomService {
    suspend fun storeUserFriendsToRoom(friends: List<UserInstance?>)
    suspend fun storeNewsToRoom(news: List<NewsInstance>)
    suspend fun storeNotificationsToRoom(notifications: List<NotificationInstance>)
}