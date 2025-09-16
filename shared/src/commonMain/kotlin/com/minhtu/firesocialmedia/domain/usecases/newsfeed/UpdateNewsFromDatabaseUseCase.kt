package com.minhtu.firesocialmedia.domain.usecases.newsfeed

import com.minhtu.firesocialmedia.domain.entity.news.NewsInstance
import com.minhtu.firesocialmedia.domain.repository.NewsRepository

class UpdateNewsFromDatabaseUseCase(
    private val newsRepository: NewsRepository
) {
    suspend operator fun invoke(newContent : String,
                                newImage : String,
                                newVideo : String,
                                new: NewsInstance) : Boolean {
        return newsRepository.updateNewsFromDatabase(
            newContent,
            newImage,
            newVideo,
            new
        )
    }
}