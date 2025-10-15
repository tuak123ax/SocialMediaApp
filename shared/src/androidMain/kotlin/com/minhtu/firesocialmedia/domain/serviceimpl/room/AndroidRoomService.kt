package com.minhtu.firesocialmedia.domain.serviceimpl.room

import com.minhtu.firesocialmedia.data.local.dao.NewDao
import com.minhtu.firesocialmedia.data.local.dao.NotificationDao
import com.minhtu.firesocialmedia.data.local.dao.UserDao
import com.minhtu.firesocialmedia.data.local.entity.NewEntity
import com.minhtu.firesocialmedia.data.local.entity.NotificationEntity
import com.minhtu.firesocialmedia.data.local.entity.UserEntity
import com.minhtu.firesocialmedia.data.local.service.room.RoomService

class AndroidRoomService(
    private val userDao: UserDao,
    private val newDao: NewDao,
    private val notificationDao: NotificationDao
) : RoomService {
    override suspend fun storeUserFriendsToRoom(friends: List<UserEntity?>) {
        val entities = friends.mapNotNull { it }
        if(entities.isNotEmpty()) {
            userDao.addAll(entities)
        }
    }

    override suspend fun storeNewsToRoom(news: List<NewEntity>) {
        if(news.isNotEmpty()) {
            newDao.addAll(news)
        }
    }

    override suspend fun storeNotificationsToRoom(notifications: List<NotificationEntity>) {
        if(notifications.isNotEmpty()) {
            notificationDao.addAll(notifications)
        }
    }

}