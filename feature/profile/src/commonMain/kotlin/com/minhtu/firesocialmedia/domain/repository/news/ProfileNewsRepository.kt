package com.minhtu.firesocialmedia.domain.repository.news

import com.minhtu.firesocialmedia.profile.entity.news.NewsInstance
import com.minhtu.firesocialmedia.profile.entity.news.ProfileNewsPage

interface ProfileNewsRepository {
    suspend fun getNew(newId: String): NewsInstance?
    suspend fun getNewsByUser(posterId: String, number: Int, lastTimePosted: Double?, lastKey: String?): ProfileNewsPage
    suspend fun deleteNewsFromDatabase(new: NewsInstance): Boolean
    suspend fun updateLikeCountForNew(newsId: String, value: Int)
    suspend fun deletePollFromDatabase(newsId: String, pollId: String, groupId: String): Boolean
}
