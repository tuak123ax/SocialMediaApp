package com.minhtu.firesocialmedia.domain.usecases.sync

import com.minhtu.firesocialmedia.domain.entity.news.NewsInstance
import com.minhtu.firesocialmedia.domain.repository.CommonDbRepository

class LoadNewsPostedWhenOfflineUseCase(
    private val commonDbRepository: CommonDbRepository
){
    suspend operator fun invoke() : List<NewsInstance> {
        return commonDbRepository.loadNewsPostedWhenOffline()
    }
}