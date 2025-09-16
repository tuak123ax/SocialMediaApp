package com.minhtu.firesocialmedia.data.repository

import com.minhtu.firesocialmedia.data.constant.DataConstant
import com.minhtu.firesocialmedia.data.mapper.home.toDomain
import com.minhtu.firesocialmedia.data.mapper.news.toDomain
import com.minhtu.firesocialmedia.data.mapper.news.toDto
import com.minhtu.firesocialmedia.domain.entity.home.LatestNewsResult
import com.minhtu.firesocialmedia.domain.entity.news.NewsInstance
import com.minhtu.firesocialmedia.domain.repository.NewsRepository
import com.minhtu.firesocialmedia.data.remote.service.database.DatabaseService

class NewsRepositoryImpl(
    private val databaseService: DatabaseService
) : NewsRepository {
    override suspend fun getNew(newId: String): NewsInstance? {
        return databaseService.getNew(newId)?.toDomain()
    }

    override suspend fun getLatestNews(
        number: Int,
        lastTimePosted: Double?,
        lastKey: String?
    ): LatestNewsResult? {
        return databaseService.getLatestNews(
            10,
            lastTimePosted,
            lastKey,
            DataConstant.NEWS_PATH).toDomain()
    }

    override suspend fun deleteNewsFromDatabase(
        new: NewsInstance
    ) {
        databaseService.deleteNewsFromDatabase(DataConstant.NEWS_PATH, new.toDto())
    }

    override suspend fun updateNewsFromDatabase(
        newContent: String,
        newImage: String,
        newVideo: String,
        new: NewsInstance
    ): Boolean {
        return databaseService.updateNewsFromDatabase(
            DataConstant.NEWS_PATH,
            newContent,
            newImage,
            newVideo,
            new.toDto())
    }
}