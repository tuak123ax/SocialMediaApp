package com.minhtu.firesocialmedia.domain.usecases.home

import com.minhtu.firesocialmedia.domain.entity.news.NewsInstance
import com.minhtu.firesocialmedia.domain.repository.NewsRepository

class DeleteNewsFromDatabaseUseCase(
    private val newsRepository: NewsRepository
) {
    suspend operator fun invoke(new: NewsInstance) {
        newsRepository.deleteNewsFromDatabase(new)
    }
}