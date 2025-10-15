package com.minhtu.firesocialmedia.data.repository

import com.minhtu.firesocialmedia.data.local.mapper.room.toNewEntity
import com.minhtu.firesocialmedia.data.local.mapper.room.toNotificationEntity
import com.minhtu.firesocialmedia.data.local.mapper.room.toUserEntity
import com.minhtu.firesocialmedia.data.local.service.crypto.CryptoService
import com.minhtu.firesocialmedia.data.local.service.room.RoomService
import com.minhtu.firesocialmedia.domain.entity.news.NewsInstance
import com.minhtu.firesocialmedia.domain.entity.notification.NotificationInstance
import com.minhtu.firesocialmedia.domain.entity.user.UserInstance
import com.minhtu.firesocialmedia.domain.repository.LocalRepository

class LocalRepositoryImpl(
    private val cryptoService: CryptoService,
    private val roomService: RoomService
) : LocalRepository {
    override suspend fun getFCMToken(): String {
        return cryptoService.getFCMToken()
    }

    override suspend fun storeUserFriendsToRoom(friends: List<UserInstance?>) {
        roomService.storeUserFriendsToRoom(friends.toUserEntity())
    }

    override suspend fun storeNewsToRoom(news: List<NewsInstance>) {
        roomService.storeNewsToRoom(news.toNewEntity())
    }

    override suspend fun storeNotificationsToRoom(notifications: List<NotificationInstance>) {
        roomService.storeNotificationsToRoom(notifications.toNotificationEntity())
    }
}