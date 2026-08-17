package com.minhtu.firesocialmedia.domain.usecases.newsfeed

import com.minhtu.firesocialmedia.domain.repository.NewsRepository
import com.minhtu.firesocialmedia.data.remote.mapper.settings.home.toDomain
import com.minhtu.firesocialmedia.domain.entity.settings.home.PollObject

class FetchPollUseCase(private val newsRepository: NewsRepository) {
    suspend operator fun invoke(pollId: String): PollObject? {
        return newsRepository.fetchPoll(pollId)?.toDomain()
    }
}
