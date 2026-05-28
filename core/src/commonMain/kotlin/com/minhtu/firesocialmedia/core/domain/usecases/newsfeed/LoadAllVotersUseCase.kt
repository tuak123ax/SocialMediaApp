package com.minhtu.firesocialmedia.core.domain.usecases.newsfeed

import com.minhtu.firesocialmedia.core.domain.repository.NewsRepository

class LoadAllVotersUseCase(private val newsRepository: NewsRepository) {
    suspend operator fun invoke(pollId: String): Map<String, List<Int>> {
        return newsRepository.loadAllVoters(pollId)
    }
}

