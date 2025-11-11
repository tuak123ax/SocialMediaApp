package com.minhtu.firesocialmedia.domain.usecases.newsfeed

import com.minhtu.firesocialmedia.domain.entity.news.NewsInstance
import com.minhtu.firesocialmedia.domain.repository.CommonDbRepository

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