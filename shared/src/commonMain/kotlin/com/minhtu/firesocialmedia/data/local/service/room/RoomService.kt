package com.minhtu.firesocialmedia.data.local.service.room

import com.minhtu.firesocialmedia.data.local.entity.NewsEntity
import com.minhtu.firesocialmedia.data.local.entity.NotificationEntity
import com.minhtu.firesocialmedia.data.local.entity.UserEntity
import com.minhtu.firesocialmedia.domain.entity.news.NewsInstance

interface RoomService {
    suspend fun storeUserFriendsToRoom(friends: List<UserEntity?>)
    suspend fun storeUserFriendToRoom(friend: UserEntity)
    suspend fun storeNewsToRoom(news: List<NewsEntity>)
    suspend fun storeNotificationsToRoom(notifications: List<NotificationEntity>)
    suspend fun getUserFromRoom(userId: String) : UserEntity?
    suspend fun getAllNotifications() : List<NotificationEntity>
    suspend fun getFirstPage(number: Int) : List<NewsEntity>
    suspend fun getPageAfter(number: Int, lastTimePosted: Long, lastKey: String?): List<NewsEntity>
    suspend fun getNewById(newId: String): NewsEntity?
}