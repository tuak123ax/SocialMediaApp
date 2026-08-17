package com.minhtu.firesocialmedia.domain.usecases.newsfeed.group

import com.minhtu.firesocialmedia.domain.repository.news.GroupNewsRepository

class DeletePollUseCase(
    private val newsRepository: GroupNewsRepository
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
