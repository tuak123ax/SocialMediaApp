package com.minhtu.firesocialmedia.domain.usecases.notification.home

import com.minhtu.firesocialmedia.home.entity.news.NewsInstance
import com.minhtu.firesocialmedia.domain.repository.NewsRepository

class FindNewByIdInDbUseCase(
    private val newsRepository : NewsRepository
) {
    suspend operator fun invoke(newId : String) : NewsInstance {
        return newsRepository.getNew(newId) ?: NewsInstance()
    }
}
