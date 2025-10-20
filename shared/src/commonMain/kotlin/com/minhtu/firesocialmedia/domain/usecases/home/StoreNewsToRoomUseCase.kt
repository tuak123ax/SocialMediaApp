package com.minhtu.firesocialmedia.domain.usecases.home

import com.minhtu.firesocialmedia.domain.entity.news.NewsInstance
import com.minhtu.firesocialmedia.domain.repository.LocalRepository

class StoreNewsToRoomUseCase(
    private val localRepository: LocalRepository
) {
    suspend operator fun invoke(news: List<NewsInstance>) {
        localRepository.storeNewsToRoom(news)
    }
}