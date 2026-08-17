package com.minhtu.firesocialmedia.domain.usecases.newsfeed.group

import com.minhtu.firesocialmedia.domain.repository.news.GroupNewsRepository

class SubmitVoteUseCase(private val newsRepository: GroupNewsRepository) {
    suspend operator fun invoke(
        pollId: String,
        userId: String,
        selectedIndices: List<Int>,
        previousIndices: List<Int>
    ): Boolean {
        return newsRepository.submitVote(pollId, userId, selectedIndices, previousIndices)
    }
}
