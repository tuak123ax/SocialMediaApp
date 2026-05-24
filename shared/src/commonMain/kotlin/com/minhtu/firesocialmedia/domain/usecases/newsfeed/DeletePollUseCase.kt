package com.minhtu.firesocialmedia.domain.usecases.newsfeed

import com.minhtu.firesocialmedia.domain.repository.NewsRepository

class DeletePollUseCase(
    private val newsRepository: NewsRepository
) {
    /**
     * Atomically deletes:
     *  - /groups/{groupId}/posts/{newsId}  — the feed index entry in the group
     *  - /polls/{pollId}                   — the full poll data
     *  - /pollVotes/{pollId}               — all votes cast on this poll
     */
    suspend operator fun invoke(newsId: String, pollId: String, groupId: String): Boolean {
        return newsRepository.deletePollFromDatabase(newsId, pollId, groupId)
    }
}


