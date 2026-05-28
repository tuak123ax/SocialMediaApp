package com.minhtu.firesocialmedia.core.domain.usecases.newsfeed

import com.minhtu.firesocialmedia.core.domain.repository.NewsRepository

class SubmitVoteUseCase(private val newsRepository: NewsRepository) {
    suspend operator fun invoke(
        pollId: String,
        userId: String,
        selectedIndices: List<Int>,
        previousIndices: List<Int>
    ): Boolean {
        return newsRepository.submitVote(pollId, userId, selectedIndices, previousIndices)
    }
}

