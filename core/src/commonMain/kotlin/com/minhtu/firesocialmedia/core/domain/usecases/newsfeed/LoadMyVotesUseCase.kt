package com.minhtu.firesocialmedia.core.domain.usecases.newsfeed

import com.minhtu.firesocialmedia.core.domain.repository.NewsRepository

class LoadMyVotesUseCase(private val newsRepository: NewsRepository) {
    suspend operator fun invoke(pollId: String, userId: String): List<Int> {
        return newsRepository.loadMyVotes(pollId, userId)
    }
}

