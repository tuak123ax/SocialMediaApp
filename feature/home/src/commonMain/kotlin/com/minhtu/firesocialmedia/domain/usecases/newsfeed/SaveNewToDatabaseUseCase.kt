package com.minhtu.firesocialmedia.domain.usecases.newsfeed

import com.minhtu.firesocialmedia.home.entity.news.NewsInstance
import com.minhtu.firesocialmedia.domain.repository.HomeDbRepository

class SaveNewToDatabaseUseCase(
    private val homeDbRepository: HomeDbRepository
) {
    suspend operator fun invoke(
        instance : NewsInstance) : Boolean {
        return homeDbRepository.saveNewToDatabase(
            instance
        )
    }
}
