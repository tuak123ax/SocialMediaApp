package com.minhtu.firesocialmedia.data.repository

import com.minhtu.firesocialmedia.domain.repository.LocalRepository
import com.minhtu.firesocialmedia.data.local.service.crypto.CryptoService
import com.minhtu.firesocialmedia.data.local.service.room.RoomService
import com.minhtu.firesocialmedia.domain.entity.news.NewsInstance
import com.minhtu.firesocialmedia.domain.entity.notification.NotificationInstance
import com.minhtu.firesocialmedia.domain.entity.user.UserInstance

class LocalRepositoryImpl(
    private val cryptoService: CryptoService,
    private val roomService: RoomService
) : LocalRepository {
    override suspend fun getFCMToken(): String {
        return cryptoService.getFCMToken()
    }

    override suspend fun storeUserFriendsToRoom(friends: List<UserInstance?>) {
        roomService.storeUserFriendsToRoom(friends)
    }

    override suspend fun storeNewsToRoom(news: List<NewsInstance>) {
        roomService.storeNewsToRoom(news)
    }

    override suspend fun storeNotificationsToRoom(notifications: List<NotificationInstance>) {
        roomService.storeNotificationsToRoom(notifications)
    }
}