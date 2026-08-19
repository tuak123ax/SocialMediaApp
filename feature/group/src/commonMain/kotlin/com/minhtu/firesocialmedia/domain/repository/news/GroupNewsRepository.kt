package com.minhtu.firesocialmedia.domain.repository.news

import com.minhtu.firesocialmedia.group.entity.news.NewsInstance
import com.minhtu.firesocialmedia.data.remote.dto.settings.group.PollDTO

interface GroupNewsRepository {
    suspend fun getNew(newId: String): NewsInstance?
    suspend fun deleteNewsFromDatabase(new: NewsInstance): Boolean
    suspend fun updateLikeCountForNew(newsId: String, value: Int)
    suspend fun deletePollFromDatabase(newsId: String, pollId: String, groupId: String): Boolean
    suspend fun fetchPoll(pollId: String): PollDTO?
    suspend fun loadMyVotes(pollId: String, userId: String): List<Int>
    suspend fun loadAllVoters(pollId: String): Map<String, List<Int>>
    suspend fun submitVote(pollId: String, userId: String, selectedIndices: List<Int>, previousIndices: List<Int>): Boolean
    suspend fun createPoll(poll: PollDTO, newsId: String, groupId: String): Boolean
}
