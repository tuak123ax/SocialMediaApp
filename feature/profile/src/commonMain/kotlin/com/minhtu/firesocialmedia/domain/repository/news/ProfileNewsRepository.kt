package com.minhtu.firesocialmedia.domain.repository.news

import com.minhtu.firesocialmedia.profile.entity.news.NewsInstance

interface ProfileNewsRepository {
    suspend fun getNew(newId: String): NewsInstance?
    suspend fun deleteNewsFromDatabase(new: NewsInstance): Boolean
    suspend fun updateLikeCountForNew(newsId: String, value: Int)
    suspend fun deletePollFromDatabase(newsId: String, pollId: String, groupId: String): Boolean
}
