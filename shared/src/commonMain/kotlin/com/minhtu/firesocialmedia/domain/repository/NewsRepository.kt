package com.minhtu.firesocialmedia.domain.repository

import com.minhtu.firesocialmedia.domain.entity.home.LatestNewsResult
import com.minhtu.firesocialmedia.domain.entity.news.NewsInstance

interface NewsRepository {
    suspend fun getNew(newId: String) : NewsInstance?
    suspend fun getLatestNews(number : Int,
                              lastTimePosted : Double?,
                              lastKey: String?,
                              path: String) : LatestNewsResult?
    suspend fun deleteNewsFromDatabase(path : String,
                                       new: NewsInstance)
    suspend fun updateNewsFromDatabase(
        path: String,
        newContent: String,
        newImage: String,
        newVideo : String,
        new: NewsInstance
    ) : Boolean
}