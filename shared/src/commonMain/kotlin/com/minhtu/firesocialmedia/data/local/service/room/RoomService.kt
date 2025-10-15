package com.minhtu.firesocialmedia.data.local.service.room

import com.minhtu.firesocialmedia.data.local.entity.NewEntity
import com.minhtu.firesocialmedia.data.local.entity.NotificationEntity
import com.minhtu.firesocialmedia.data.local.entity.UserEntity

interface RoomService {
    suspend fun storeUserFriendsToRoom(friends: List<UserEntity?>)
    suspend fun storeNewsToRoom(news: List<NewEntity>)
    suspend fun storeNotificationsToRoom(notifications: List<NotificationEntity>)
}