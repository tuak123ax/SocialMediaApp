package com.minhtu.firesocialmedia.domain.interactor.home

import com.minhtu.firesocialmedia.domain.entity.home.LatestNewsResult
import com.minhtu.firesocialmedia.domain.entity.news.NewsInstance

interface NewsInteractor {
    suspend fun pageLatest(number : Int,
                           lastTimePosted : Double?,
                           lastKey: String?,
                           path: String): LatestNewsResult?
    suspend fun like(id: String,
                     path: String,
                     externalPath: String,
                     value: Int)
    suspend fun unlike(id: String,
                       path: String,
                       externalPath: String,
                       value: Int)
    suspend fun delete(path: String, new: NewsInstance)
}