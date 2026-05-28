package com.minhtu.firesocialmedia.core.domain.usecases.home

import com.minhtu.firesocialmedia.core.domain.entity.home.LatestNewsResult
import com.minhtu.firesocialmedia.core.domain.repository.NewsRepository

class GetLatestNewsUseCase(
    private val newsRepository: NewsRepository
) {
    suspend operator fun invoke(number : Int,
                                lastTimePosted : Double?,
                                lastKey: String?) : LatestNewsResult? {
        val result = newsRepository.getLatestNews(number, lastTimePosted, lastKey)

        return result?.copy(
            news = result.news?.filter { news ->
                news.message.isNotBlank() ||
                        news.image.isNotBlank() ||
                        news.video.isNotBlank()
            }
        )
    }
}