package com.minhtu.firesocialmedia.domain.usecases.newsfeed

import com.minhtu.firesocialmedia.domain.repository.NewsRepository

class LoadMyVotesUseCase(private val newsRepository: NewsRepository) {
    suspend operator fun invoke(pollId: String, userId: String): List<Int> {
        return newsRepository.loadMyVotes(pollId, userId)
    }
}
