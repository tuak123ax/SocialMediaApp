package com.minhtu.firesocialmedia.domain.usecases.newsfeed.group

import com.minhtu.firesocialmedia.domain.repository.news.GroupNewsRepository

class LoadMyVotesUseCase(private val newsRepository: GroupNewsRepository) {
    suspend operator fun invoke(pollId: String, userId: String): List<Int> {
        return newsRepository.loadMyVotes(pollId, userId)
    }
}
