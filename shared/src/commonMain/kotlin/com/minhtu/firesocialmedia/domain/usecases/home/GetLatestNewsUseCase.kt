package com.minhtu.firesocialmedia.domain.usecases.home

import com.minhtu.firesocialmedia.domain.entity.home.LatestNewsResult
import com.minhtu.firesocialmedia.domain.repository.NewsRepository

class GetLatestNewsUseCase(
    private val newsRepository: NewsRepository
) {
    suspend operator fun invoke(number : Int,
                                lastTimePosted : Double?,
                                lastKey: String?,
                                path: String) : LatestNewsResult? {
        return newsRepository.getLatestNews(number, lastTimePosted, lastKey, path)
    }
}