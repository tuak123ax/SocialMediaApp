package com.minhtu.firesocialmedia.core.domain.usecases.home

import com.minhtu.firesocialmedia.core.domain.entity.news.NewsInstance
import com.minhtu.firesocialmedia.core.domain.repository.NewsRepository

class DeleteNewsFromDatabaseUseCase(
    private val newsRepository: NewsRepository
) {
    suspend operator fun invoke(new: NewsInstance) {
        newsRepository.deleteNewsFromDatabase(new)
    }
}