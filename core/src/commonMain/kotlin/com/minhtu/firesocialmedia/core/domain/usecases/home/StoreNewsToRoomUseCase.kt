package com.minhtu.firesocialmedia.core.domain.usecases.home

import com.minhtu.firesocialmedia.core.domain.entity.news.NewsInstance
import com.minhtu.firesocialmedia.core.domain.repository.LocalRepository

class StoreNewsToRoomUseCase(
    private val localRepository: LocalRepository
) {
    suspend operator fun invoke(news: List<NewsInstance>) {
        localRepository.storeNewsToRoom(news)
    }
}