package com.minhtu.firesocialmedia.core.domain.usecases.newsfeed

import com.minhtu.firesocialmedia.core.domain.entity.news.NewsInstance
import com.minhtu.firesocialmedia.core.domain.repository.CommonDbRepository

class SaveNewToDatabaseUseCase(
    private val commonDbRepository: CommonDbRepository
) {
    suspend operator fun invoke(
        instance : NewsInstance) : Boolean {
        return commonDbRepository.saveNewToDatabase(
            instance
        )
    }
}