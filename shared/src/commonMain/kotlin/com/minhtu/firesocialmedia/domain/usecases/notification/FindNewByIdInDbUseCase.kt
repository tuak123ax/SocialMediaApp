package com.minhtu.firesocialmedia.domain.usecases.notification

import com.minhtu.firesocialmedia.domain.entity.news.NewsInstance
import com.minhtu.firesocialmedia.domain.repository.NewsRepository

class FindNewByIdInDbUseCase(
    private val newsRepository : NewsRepository
) {
    suspend operator fun invoke(newId : String) : NewsInstance? {
        return newsRepository.getNew(newId)
    }
}