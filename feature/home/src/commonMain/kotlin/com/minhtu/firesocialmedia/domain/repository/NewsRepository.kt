package com.minhtu.firesocialmedia.domain.repository

import com.minhtu.firesocialmedia.home.entity.news.NewsInstance
import com.minhtu.firesocialmedia.domain.entity.home.LatestNewsResult
import com.minhtu.firesocialmedia.data.remote.dto.settings.home.PollDTO

interface NewsRepository {
    suspend fun deletePollFromDatabase(newsId: String, pollId: String, groupId: String): Boolean
    suspend fun fetchPoll(pollId: String): PollDTO?
    suspend fun loadMyVotes(pollId: String, userId: String): List<Int>
    suspend fun loadAllVoters(pollId: String): Map<String, List<Int>>
    suspend fun submitVote(pollId: String, userId: String, selectedIndices: List<Int>, previousIndices: List<Int>): Boolean
    suspend fun getNew(newId: String): NewsInstance?
    suspend fun updateLikeCountForNew(newsId: String, value: Int)
    suspend fun deleteNewsFromDatabase(new: NewsInstance)
    suspend fun updateNewsFromDatabase(
        newContent: String,
        newImage: String,
        newVideo: String,
        new: NewsInstance
    ): Boolean
    suspend fun getLatestNews(number : Int,
                              lastTimePosted : Double?,
                              lastKey: String?) : LatestNewsResult?
}
