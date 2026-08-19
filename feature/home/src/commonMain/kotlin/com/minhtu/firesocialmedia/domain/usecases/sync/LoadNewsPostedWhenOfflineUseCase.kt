package com.minhtu.firesocialmedia.domain.usecases.sync

import com.minhtu.firesocialmedia.home.entity.news.NewsInstance
import com.minhtu.firesocialmedia.domain.repository.HomeDbRepository

class LoadNewsPostedWhenOfflineUseCase(
    private val homeDbRepository: HomeDbRepository
){
    suspend operator fun invoke() : List<NewsInstance> {
        return homeDbRepository.loadNewsPostedWhenOffline()
    }
}
