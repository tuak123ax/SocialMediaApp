package com.minhtu.firesocialmedia.domain.interactor.home

import com.minhtu.firesocialmedia.domain.entity.home.LatestNewsResult
import com.minhtu.firesocialmedia.domain.entity.news.NewsInstance

interface NewsInteractor {
    suspend fun pageLatest(number : Int,
                           lastTimePosted : Double?,
                           lastKey: String?): LatestNewsResult?
    suspend fun like(id: String,
                     value: Int)
    suspend fun unlike(id: String,
                       value: Int)
    suspend fun delete(new: NewsInstance)

    suspend fun storeNewsToRoom(news : List<NewsInstance>)
}