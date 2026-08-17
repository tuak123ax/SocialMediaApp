package com.minhtu.firesocialmedia.domain.repository.news

import com.minhtu.firesocialmedia.notification.entity.news.NewsInstance

interface NotificationNewsRepository {
    suspend fun getNew(newId: String): NewsInstance?
}
