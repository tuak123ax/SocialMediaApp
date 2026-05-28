package com.minhtu.firesocialmedia.core.domain.usecases.notification

import com.minhtu.firesocialmedia.core.domain.entity.news.NewsInstance
import com.minhtu.firesocialmedia.core.domain.repository.NewsRepository

class FindNewByIdInDbUseCase(
    private val newsRepository : NewsRepository
) {
    suspend operator fun invoke(newId : String) : NewsInstance {
        return newsRepository.getNew(newId)?: NewsInstance()
    }
}