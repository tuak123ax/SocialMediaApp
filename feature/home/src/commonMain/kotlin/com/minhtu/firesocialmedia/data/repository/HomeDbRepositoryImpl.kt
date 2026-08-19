package com.minhtu.firesocialmedia.data.repository

import com.minhtu.firesocialmedia.data.local.service.room.HomeNewsRoomService
import com.minhtu.firesocialmedia.data.local.service.room.HomeUserRoomService
import com.minhtu.firesocialmedia.constants.home.DataConstant
import com.minhtu.firesocialmedia.data.remote.mapper.news.toDto
import com.minhtu.firesocialmedia.data.remote.service.database.HomeDatabaseService
import com.minhtu.firesocialmedia.network.home.NetworkMonitor
import com.minhtu.firesocialmedia.home.entity.news.NewsInstance
import com.minhtu.firesocialmedia.domain.repository.HomeDbRepository
import com.minhtu.firesocialmedia.platform.logMessage
import kotlinx.coroutines.flow.first

class HomeDbRepositoryImpl(
    private val homeDatabaseService: HomeDatabaseService,
    private val localDatabaseService: HomeNewsRoomService,
    private val userRoomService: HomeUserRoomService,
    private val networkMonitor: NetworkMonitor
) : HomeDbRepository {
    override suspend fun saveLikedPost(
        id: String,
        value: HashMap<String, Int>
    ): Boolean {
        val isOnline = networkMonitor.isOnline.first()
        if (isOnline) {
            return homeDatabaseService.saveValueToDatabase(
                id,
                DataConstant.USER_PATH,
                value,
                DataConstant.LIKED_POSTS_PATH
            )
        } else {
            try {
                localDatabaseService.saveLikedPost(
                    value
                )
                return true
            } catch (ex: Exception) {
                logMessage("saveLikedPost", { "Exception when saveLikedPost: ${ex.message}" })
                return false
            }
        }
    }

    override suspend fun saveNewToDatabase(
        instance: NewsInstance
    ): Boolean {
        val isOnline = networkMonitor.isOnline.first()
        if (isOnline) {
            return homeDatabaseService.saveNewToDatabase(
                instance.id,
                DataConstant.NEWS_PATH,
                instance.toDto()
            )
        } else {
            try {
                localDatabaseService.saveNews(
                    instance
                )
                return true
            } catch (ex: Exception) {
                logMessage(
                    "saveInstanceToDatabase",
                    { "Exception when saveInstanceToDatabase: ${ex.message}" })
                return false
            }
        }
    }

    override suspend fun updateLikeCountForNewInDatabase(
        id: String,
        value: Int
    ) {
        homeDatabaseService.updateCountValueInDatabase(
            id,
            DataConstant.NEWS_PATH,
            DataConstant.LIKED_COUNT_PATH,
            value
        )
    }

    override suspend fun syncLikedPosts(currentUserId: String): Boolean {
        if (!localDatabaseService.hasLikedPost()) {
            return true
        }
        val likedMap = localDatabaseService.getAllLikedPosts()
        val likedOk = runCatching {
            homeDatabaseService.saveValueToDatabase(
                currentUserId,
                DataConstant.USER_PATH,
                likedMap,
                DataConstant.LIKED_POSTS_PATH
            )
        }.getOrElse { false }
        clearLikedPosts()
        return likedOk
    }

    override suspend fun clearLikedPosts() {
        localDatabaseService.clearLikedPosts()
    }

    override suspend fun loadNewsPostedWhenOffline(): List<NewsInstance> {
        return localDatabaseService.loadNewsPostedWhenOffline()
    }

    override suspend fun deleteAllDraftPosts(): Boolean {
        return try {
            localDatabaseService.deleteAllDraftPosts()
            true
        } catch (ex: Exception) {
            false
        }
    }

    override suspend fun deleteDraftPost(newId: String): Boolean {
        return try {
            localDatabaseService.deleteDraftPost(newId)
            true
        } catch (ex: Exception) {
            false
        }
    }

    override suspend fun clearLocalFriends() {
        userRoomService.clearLocalFriends()
    }

    override suspend fun saveNewToGroup(groupId: String, instance: NewsInstance): Boolean {
        val isOnline = networkMonitor.isOnline.first()
        if (!isOnline) return false
        return homeDatabaseService.saveNewToGroup(groupId, instance.toDto())
    }

    override suspend fun getAllMembersInGroup(groupId: String): HashMap<String, String> {
        return homeDatabaseService.getAllMembersInGroup(groupId)
    }

    override suspend fun isGroupNotificationOnForUser(userId: String, groupId: String): Boolean {
        return homeDatabaseService.isGroupNotificationOnForUser(userId, groupId)
    }
}
