package com.minhtu.firesocialmedia.core.domain.usecases.sync

import com.minhtu.firesocialmedia.core.domain.entity.news.NewsInstance
import com.minhtu.firesocialmedia.core.domain.repository.CommonDbRepository

class LoadNewsPostedWhenOfflineUseCase(
    private val commonDbRepository: CommonDbRepository
){
    suspend operator fun invoke() : List<NewsInstance> {
        return commonDbRepository.loadNewsPostedWhenOffline()
    }
}