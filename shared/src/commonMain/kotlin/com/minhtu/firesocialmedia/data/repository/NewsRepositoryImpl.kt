package com.minhtu.firesocialmedia.data.repository

import com.minhtu.firesocialmedia.constants.Constants
import com.minhtu.firesocialmedia.data.mapper.home.toDomain
import com.minhtu.firesocialmedia.data.mapper.news.toDomain
import com.minhtu.firesocialmedia.data.mapper.news.toDto
import com.minhtu.firesocialmedia.domain.entity.home.LatestNewsResult
import com.minhtu.firesocialmedia.domain.entity.news.NewsInstance
import com.minhtu.firesocialmedia.domain.repository.NewsRepository
import com.minhtu.firesocialmedia.domain.service.database.DatabaseService

class NewsRepositoryImpl(
    private val databaseService: DatabaseService
) : NewsRepository {
    override suspend fun getNew(newId: String): NewsInstance? {
        return databaseService.getNew(newId)?.toDomain()
    }

    override suspend fun getLatestNews(
        number: Int,
        lastTimePosted: Double?,
        lastKey: String?,
        path: String
    ): LatestNewsResult? {
        return databaseService.getLatestNews(
            10,
            lastTimePosted,
            lastKey,
            Constants.NEWS_PATH).toDomain()
    }

    override suspend fun deleteNewsFromDatabase(
        path: String,
        new: NewsInstance
    ) {
        databaseService.deleteNewsFromDatabase(Constants.NEWS_PATH, new.toDto())
    }

    override suspend fun updateNewsFromDatabase(
        path: String,
        newContent: String,
        newImage: String,
        newVideo: String,
        new: NewsInstance
    ): Boolean {
        return databaseService.updateNewsFromDatabase(
            path,
            newContent,
            newImage,
            newVideo,
            new.toDto())
    }
}