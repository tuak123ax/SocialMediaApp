package com.minhtu.firesocialmedia.core.domain.usecases.newsfeed

import com.minhtu.firesocialmedia.core.domain.entity.settings.PollObject
import com.minhtu.firesocialmedia.core.domain.repository.NewsRepository

class FetchPollUseCase(private val newsRepository: NewsRepository) {
    suspend operator fun invoke(pollId: String): PollObject? {
        return newsRepository.fetchPoll(pollId)
    }
}

