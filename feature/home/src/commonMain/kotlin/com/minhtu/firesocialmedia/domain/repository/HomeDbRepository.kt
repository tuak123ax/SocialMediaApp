package com.minhtu.firesocialmedia.domain.repository

import com.minhtu.firesocialmedia.home.entity.news.NewsInstance

interface HomeDbRepository {
    suspend fun saveLikedPost(id: String, value: HashMap<String, Int>): Boolean
    suspend fun saveNewToDatabase(instance: NewsInstance): Boolean
    suspend fun updateLikeCountForNewInDatabase(id: String, value: Int)
    suspend fun syncLikedPosts(currentUserId: String): Boolean
    suspend fun clearLikedPosts()
    suspend fun loadNewsPostedWhenOffline(): List<NewsInstance>
    suspend fun deleteAllDraftPosts(): Boolean
    suspend fun deleteDraftPost(newId: String): Boolean
    suspend fun clearLocalFriends()
    suspend fun saveNewToGroup(groupId: String, instance: NewsInstance): Boolean
    suspend fun getAllMembersInGroup(groupId: String): HashMap<String, String>
    suspend fun isGroupNotificationOnForUser(userId: String, groupId: String): Boolean
}
