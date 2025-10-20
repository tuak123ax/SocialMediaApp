package com.minhtu.firesocialmedia.domain.serviceimpl.room

import com.minhtu.firesocialmedia.data.local.dao.NewsDao
import com.minhtu.firesocialmedia.data.local.dao.NotificationDao
import com.minhtu.firesocialmedia.data.local.dao.UserDao
import com.minhtu.firesocialmedia.data.local.entity.NewsEntity
import com.minhtu.firesocialmedia.data.local.entity.NotificationEntity
import com.minhtu.firesocialmedia.data.local.entity.UserEntity
import com.minhtu.firesocialmedia.data.local.service.room.RoomService

class AndroidRoomService(
    private val userDao: UserDao,
    private val newsDao: NewsDao,
    private val notificationDao: NotificationDao
) : RoomService {
    override suspend fun storeUserFriendsToRoom(friends: List<UserEntity?>) {
        val entities = friends.mapNotNull { it }
        if(entities.isNotEmpty()) {
            userDao.addAll(entities)
        }
    }

    override suspend fun storeUserFriendToRoom(friend: UserEntity) {
        userDao.add(friend)
    }

    override suspend fun storeNewsToRoom(news: List<NewsEntity>) {
        if(news.isNotEmpty()) {
            newsDao.addAll(news)
        }
    }

    override suspend fun storeNotificationsToRoom(notifications: List<NotificationEntity>) {
        if(notifications.isNotEmpty()) {
            notificationDao.addAll(notifications)
        }
    }

    override suspend fun getUserFromRoom(userId: String): UserEntity? {
        return userDao.getById(userId)
    }

    override suspend fun getAllNotifications(): List<NotificationEntity> {
        return notificationDao.getAll()
    }

    override suspend fun getFirstPage(number: Int) : List<NewsEntity> {
        return newsDao.firstPage(number)
    }

    override suspend fun getPageAfter(
        number: Int,
        lastTimePosted: Long,
        lastKey: String?
    ): List<NewsEntity> {
        return newsDao.pageAfter(number, lastTimePosted, lastKey)
    }

    override suspend fun getNewById(newId: String): NewsEntity? {
        return newsDao.getById(newId)
    }

}