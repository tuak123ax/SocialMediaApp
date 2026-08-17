package com.minhtu.firesocialmedia.data.remote.service.database

import com.minhtu.firesocialmedia.notification.data.remote.dto.news.NewsDTO
import com.minhtu.firesocialmedia.notification.data.remote.dto.user.UserDTO

interface NotificationDatabaseService {
    suspend fun getNew(newId: String, newsPath: String): NewsDTO?
    suspend fun getUser(userId: String): UserDTO?
}
