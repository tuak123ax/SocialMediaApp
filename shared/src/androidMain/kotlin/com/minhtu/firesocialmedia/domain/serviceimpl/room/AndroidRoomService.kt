package com.minhtu.firesocialmedia.domain.serviceimpl.room

import android.content.Context
import com.minhtu.firesocialmedia.data.local.service.room.RoomService
import com.minhtu.firesocialmedia.domain.entity.news.NewsInstance
import com.minhtu.firesocialmedia.domain.entity.notification.NotificationInstance
import com.minhtu.firesocialmedia.domain.entity.user.UserInstance

class AndroidRoomService(
    private val context: Context
) : RoomService {
    override suspend fun storeUserFriendsToRoom(friends: List<UserInstance?>) {

    }

    override suspend fun storeNewsToRoom(news: List<NewsInstance>) {
    }

    override suspend fun storeNotificationsToRoom(notifications: List<NotificationInstance>) {
    }
}