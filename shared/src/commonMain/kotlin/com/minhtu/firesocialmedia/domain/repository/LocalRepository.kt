package com.minhtu.firesocialmedia.domain.repository

import com.minhtu.firesocialmedia.domain.entity.news.NewsInstance
import com.minhtu.firesocialmedia.domain.entity.notification.NotificationInstance
import com.minhtu.firesocialmedia.domain.entity.user.UserInstance

interface LocalRepository {
    suspend fun getFCMToken() : String
    suspend fun storeUserFriendsToRoom(friends: List<UserInstance?>)
    suspend fun storeNewsToRoom(news: List<NewsInstance>)
    suspend fun storeNotificationsToRoom(notifications: List<NotificationInstance>)
    suspend fun getUserFromRoom(userId: String) : UserInstance?
}