package com.minhtu.firesocialmedia.domain.usecases.newsfeed

import com.minhtu.firesocialmedia.domain.entity.settings.PollObject
import com.minhtu.firesocialmedia.domain.repository.NewsRepository

class FetchPollUseCase(private val newsRepository: NewsRepository) {
    suspend operator fun invoke(pollId: String): PollObject? {
        return newsRepository.fetchPoll(pollId)
    }
}

