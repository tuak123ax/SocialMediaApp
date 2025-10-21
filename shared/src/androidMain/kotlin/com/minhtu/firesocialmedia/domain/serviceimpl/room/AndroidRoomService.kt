package com.minhtu.firesocialmedia.domain.serviceimpl.room

import com.minhtu.firesocialmedia.data.local.dao.CommentDao
import com.minhtu.firesocialmedia.data.local.dao.NewsDao
import com.minhtu.firesocialmedia.data.local.dao.NotificationDao
import com.minhtu.firesocialmedia.data.local.dao.UserDao
import com.minhtu.firesocialmedia.data.local.entity.CommentEntity
import com.minhtu.firesocialmedia.data.local.entity.LikedPostEntity
import com.minhtu.firesocialmedia.data.local.entity.NewsEntity
import com.minhtu.firesocialmedia.data.local.entity.NotificationEntity
import com.minhtu.firesocialmedia.data.local.entity.UserEntity
import com.minhtu.firesocialmedia.data.local.service.room.RoomService
import com.minhtu.firesocialmedia.domain.entity.base.BaseNewsInstance

class AndroidRoomService(
    private val userDao: UserDao,
    private val newsDao: NewsDao,
    private val notificationDao: NotificationDao,
    private val commentDao : CommentDao
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

    override suspend fun saveLikedPost(
        value: List<LikedPostEntity>
    ) {
        return newsDao.storeAllLikedPosts(value)
    }

    override suspend fun getAllLikedPosts(): List<LikedPostEntity> {
        return newsDao.getAllLikedPosts()
    }

    override suspend fun clearLikedPosts() {
        newsDao.clearLikedPosts()
    }

    override suspend fun saveComment(
        commentEntity : CommentEntity
    ) {
        commentDao.saveComment(
            commentEntity
        )
    }

    override suspend fun getAllComments(): List<CommentEntity> {
        return commentDao.getAllComments()
    }

    override suspend fun clearComments() {
        commentDao.clear()
    }

    override suspend fun hasLikedPost(): Boolean {
        return newsDao.hasAnyLikedPosts()
    }

    override suspend fun hasComment(): Boolean {
        return commentDao.hasAnyComments()
    }

}