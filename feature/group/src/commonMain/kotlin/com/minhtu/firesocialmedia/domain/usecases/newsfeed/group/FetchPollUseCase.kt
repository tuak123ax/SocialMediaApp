package com.minhtu.firesocialmedia.domain.usecases.newsfeed.group

import com.minhtu.firesocialmedia.domain.repository.news.GroupNewsRepository
import com.minhtu.firesocialmedia.data.remote.mapper.settings.group.toDomain
import com.minhtu.firesocialmedia.domain.entity.settings.group.PollObject

class FetchPollUseCase(private val newsRepository: GroupNewsRepository) {
    suspend operator fun invoke(pollId: String): PollObject? {
        return newsRepository.fetchPoll(pollId)?.toDomain()
    }
}
