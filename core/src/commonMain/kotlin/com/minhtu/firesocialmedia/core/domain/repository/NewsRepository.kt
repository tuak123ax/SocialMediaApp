package com.minhtu.firesocialmedia.core.domain.repository

import com.minhtu.firesocialmedia.core.domain.entity.home.LatestNewsResult
import com.minhtu.firesocialmedia.core.domain.entity.news.NewsInstance
import com.minhtu.firesocialmedia.core.domain.entity.settings.PollObject

interface NewsRepository {
    suspend fun getNew(newId: String) : NewsInstance?
    suspend fun getLatestNews(number : Int,
                              lastTimePosted : Double?,
                              lastKey: String?) : LatestNewsResult?
    suspend fun deleteNewsFromDatabase(new: NewsInstance)
    suspend fun deletePollFromDatabase(newsId: String, pollId: String, groupId: String): Boolean
    suspend fun updateNewsFromDatabase(
        newContent: String,
        newImage: String,
        newVideo : String,
        new: NewsInstance
    ) : Boolean
    suspend fun fetchPoll(pollId: String): PollObject?
    suspend fun loadMyVotes(pollId: String, userId: String): List<Int>
    suspend fun loadAllVoters(pollId: String): Map<String, List<Int>>
    suspend fun submitVote(pollId: String, userId: String, selectedIndices: List<Int>, previousIndices: List<Int>): Boolean
}