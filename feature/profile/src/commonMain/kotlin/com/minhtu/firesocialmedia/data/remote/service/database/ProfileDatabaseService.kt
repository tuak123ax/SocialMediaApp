package com.minhtu.firesocialmedia.data.remote.service.database

import com.minhtu.firesocialmedia.profile.data.remote.dto.news.NewsDTO
import com.minhtu.firesocialmedia.profile.data.remote.dto.news.ProfileLatestNewsDTO
import com.minhtu.firesocialmedia.profile.data.remote.dto.user.UserDTO

interface ProfileDatabaseService {
    suspend fun getNew(newId: String, newsPath: String): NewsDTO?
    suspend fun getNewsByPoster(
        posterId: String,
        number: Int,
        lastTimePosted: Double?,
        lastKey: String?,
        newsPath: String
    ): ProfileLatestNewsDTO
    suspend fun deleteNewsFromDatabase(new: NewsDTO, newsPath: String): Boolean
    suspend fun updateLikeCountForNew(newsId: String, value: Int, newsPath: String, likedCountPath: String)
    suspend fun deletePollFromDatabase(newsId: String, pollId: String, groupPath: String, groupId: String, postsPath: String, pollPath: String, pollVotesPath: String): Boolean
    suspend fun getUser(userId: String): UserDTO?
    suspend fun saveValueToDatabase(id: String, path: String, value: HashMap<String, Int>, externalPath: String): Boolean
    suspend fun saveListToDatabase(id: String, path: String, value: ArrayList<String>, externalPath: String)
    suspend fun updateUserStringField(userId: String, fieldPath: String, value: String, userPath: String): Boolean
    suspend fun updateUserAvatar(userId: String, imageUri: String, userPath: String): Boolean
    suspend fun updateUserBackground(userId: String, imageUri: String, userPath: String): Boolean
    suspend fun anyChildMatchesFieldValue(path: String, fields: List<String>, value: String): Boolean?
}
