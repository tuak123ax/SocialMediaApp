package com.minhtu.firesocialmedia.data.repository.news

import com.minhtu.firesocialmedia.notification.entity.news.NewsInstance
import com.minhtu.firesocialmedia.constants.notification.DataConstant
import com.minhtu.firesocialmedia.notification.data.remote.mapper.news.toDomain
import com.minhtu.firesocialmedia.data.remote.service.database.NotificationDatabaseService
import com.minhtu.firesocialmedia.network.notification.NetworkMonitor
import com.minhtu.firesocialmedia.domain.repository.news.NotificationNewsRepository
import kotlinx.coroutines.flow.first

class NotificationNewsRepositoryImpl(
    private val databaseService: NotificationDatabaseService,
    private val networkMonitor: NetworkMonitor
) : NotificationNewsRepository {
    override suspend fun getNew(newId: String): NewsInstance? {
        val isOnline = networkMonitor.isOnline.first()
        return if (isOnline) {
            databaseService.getNew(newId, DataConstant.NEWS_PATH)?.toDomain()
        } else null
    }
}
