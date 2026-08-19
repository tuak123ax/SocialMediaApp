package com.minhtu.firesocialmedia.domain.usecases.newsfeed.profile

import com.minhtu.firesocialmedia.domain.repository.news.ProfileNewsRepository

class DeletePollUseCase(
    private val newsRepository: ProfileNewsRepository
) {
    suspend operator fun invoke(newsId: String, pollId: String, groupId: String): Boolean {
        return newsRepository.deletePollFromDatabase(newsId, pollId, groupId)
    }
}
