package com.minhtu.firesocialmedia.data.repository

import com.minhtu.firesocialmedia.data.local.mapper.room.toDomain
import com.minhtu.firesocialmedia.data.local.mapper.room.toDto
import com.minhtu.firesocialmedia.data.local.mapper.room.toRoomEntity
import com.minhtu.firesocialmedia.data.local.service.room.RoomService
import com.minhtu.firesocialmedia.data.remote.constant.DataConstant
import com.minhtu.firesocialmedia.data.remote.mapper.news.toDto
import com.minhtu.firesocialmedia.data.remote.service.database.DatabaseService
import com.minhtu.firesocialmedia.domain.core.NetworkMonitor
import com.minhtu.firesocialmedia.domain.entity.base.BaseNewsInstance
import com.minhtu.firesocialmedia.domain.entity.comment.CommentInstance
import com.minhtu.firesocialmedia.domain.entity.news.NewsInstance
import com.minhtu.firesocialmedia.domain.repository.CommonDbRepository
import com.minhtu.firesocialmedia.platform.logMessage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.supervisorScope
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withPermit

class CommonDbRepositoryImpl(
    private val databaseService: DatabaseService,
    private val localDatabaseService : RoomService,
    private val networkMonitor: NetworkMonitor
) : CommonDbRepository {
    override suspend fun saveLikedPost(
        id: String,
        value: HashMap<String, Int>
    ): Boolean {
        val isOnline = networkMonitor.isOnline.first()
        if(isOnline) {
            return databaseService.saveValueToDatabase(
                id,
                DataConstant.USER_PATH,
                value,
                DataConstant.LIKED_POSTS_PATH
            )
        } else {
            try{
                localDatabaseService.saveLikedPost(
                    value.toRoomEntity()
                )
                return true
            } catch(ex : Exception) {
                logMessage("saveLikedPost", { "Exception when saveLikedPost: ${ex.message}" })
                return false
            }
        }
    }

    override suspend fun saveNewToDatabase(
        instance: NewsInstance) : Boolean {
        val isOnline = networkMonitor.isOnline.first()
        if(isOnline) {
            return databaseService.saveNewToDatabase(
                instance.id,
                DataConstant.NEWS_PATH,
                instance.toDto()
            )
        } else {
            try{
                val newRoomEntity = instance.toRoomEntity()
                newRoomEntity.isNewPost = true
                localDatabaseService.saveNews(
                    newRoomEntity
                )
                return true
            } catch (ex : Exception) {
                logMessage("saveInstanceToDatabase", { "Exception when saveInstanceToDatabase: ${ex.message}" })
                return false
            }
        }
    }

    override suspend fun saveCommentToDatabase(
        selectedNewId: String,
        commentId : String,
        instance : CommentInstance
    ): Boolean {
        val isOnline = networkMonitor.isOnline.first()
        if(isOnline) {
            return databaseService.saveInstanceToDatabase(
                commentId,
                DataConstant.NEWS_PATH+"/"+selectedNewId+"/"+ DataConstant.COMMENT_PATH,
                instance
            )
        } else {
            try{
                localDatabaseService.saveComment(
                    instance.toRoomEntity(selectedNewId)
                )
                return true
            } catch (ex : Exception) {
                logMessage("saveCommentToDatabase", { "Exception when saveCommentToDatabase: ${ex.message}" })
                return false
            }
        }
    }

    override suspend fun saveSubCommentToDatabase(
        id: String,
        selectedNewId : String,
        parentCommentId : String,
        instance: BaseNewsInstance
    ): Boolean {
        return databaseService.saveInstanceToDatabase(
            id,
            DataConstant.NEWS_PATH+"/"+selectedNewId+"/"+ DataConstant.COMMENT_PATH+"/"+parentCommentId+"/"+ DataConstant.LIST_REPLIES_PATH,
            instance
        )
    }

    override suspend fun deleteCommentFromDatabase(
        selectedNewId: String,
        instance: BaseNewsInstance
    ) {
        databaseService.deleteCommentFromDatabase(
            DataConstant.NEWS_PATH + "/" +
                    selectedNewId + "/" +
                    DataConstant.COMMENT_PATH,
            instance
        )
    }

    override suspend fun deleteSubCommentFromDatabase(
        selectedNewId: String,
        parentCommentId : String,
        instance: BaseNewsInstance
    ) {
        databaseService.deleteCommentFromDatabase(
            DataConstant.NEWS_PATH + "/" +
                    selectedNewId + "/" +
                    DataConstant.COMMENT_PATH + "/" +
                    parentCommentId + "/" +
                    DataConstant.LIST_REPLIES_PATH,
            instance
        )
    }

    override suspend fun updateCommentCountForNewInDatabase(
        id: String,
        value: Int
    ) {
        databaseService.updateCountValueInDatabase(
            id,
            DataConstant.NEWS_PATH,
            DataConstant.COMMENT_COUNT_PATH,
            value
        )
    }

    override suspend fun updateReplyCountForCommentInDatabase(
        id: String,
        currentCommentId : String,
        value: Int
    ) {
        databaseService.updateCountValueInDatabase(
            id,
            DataConstant.NEWS_PATH,
            DataConstant.COMMENT_PATH + "/" + currentCommentId +"/"
                    +DataConstant.COMMENT_COUNT_PATH,
            value
        )
    }

    override suspend fun updateLikeCountForNewInDatabase(
        id: String,
        value: Int
    ) {
        databaseService.updateCountValueInDatabase(
            id,
            DataConstant.NEWS_PATH,
            DataConstant.LIKED_COUNT_PATH,
            value
        )
    }

    override suspend fun updateLikeCountForCommentInDatabase(selectedNewId: String, commentId : String, value: Int) {
        databaseService.updateCountValueInDatabase(
            selectedNewId,
            DataConstant.NEWS_PATH,
            DataConstant.COMMENT_PATH + "/" + commentId + "/" + DataConstant.LIKED_COUNT_PATH,
            value
        )
    }

    override suspend fun updateLikeCountForSubCommentInDatabase(
        selectedNewId: String,
        likedComment : String,
        parentCommentId : String,
        value: Int
    ) {
        databaseService.updateCountValueInDatabase(
            selectedNewId,
            DataConstant.NEWS_PATH,
            DataConstant.COMMENT_PATH + "/" +
                    parentCommentId + "/" +
                    DataConstant.LIST_REPLIES_PATH + "/" +
                    likedComment + "/" +
                    DataConstant.LIKED_COUNT_PATH,
            value
        )
    }

    override suspend fun saveLikedComments(
        id: String,
        value: HashMap<String, Int>
    ): Boolean {
        return databaseService.saveValueToDatabase(
            id,
            DataConstant.USER_PATH,
            value,
            DataConstant.LIKED_COMMENT_PATH
        )
    }

    override suspend fun saveFriend(id : String,
                                    value : ArrayList<String>) {
        databaseService.saveListToDatabase(
            id,
            DataConstant.USER_PATH,
            value,
            DataConstant.FRIENDS_PATH
        )
    }

    override suspend fun saveFriendRequest(id : String,
                                           value : ArrayList<String>) {
        databaseService.saveListToDatabase(
            id,
            DataConstant.USER_PATH,
            value,
            DataConstant.FRIEND_REQUESTS_PATH
        )
    }

    override suspend fun syncData(
        currentUserId: String
    ): Boolean = supervisorScope {
        var allOk = true
        try{
            // 1) Liked posts: single batched write
            if (localDatabaseService.hasLikedPost()) {
                //Has liked posts in local, sync with remote server
                val likedMap = localDatabaseService.getAllLikedPosts().toDto()
                val likedOk = runCatching {
                    databaseService.saveValueToDatabase(
                        currentUserId,
                        DataConstant.USER_PATH,
                        likedMap,
                        DataConstant.LIKED_POSTS_PATH
                    )
                }.getOrElse { false } // treat exception as failure
                allOk = allOk && likedOk
                clearLikedPosts()
            }

            // 2) Comments: fan out with bounded concurrency
            if (localDatabaseService.hasComment()) {
                //Has comments in local, sync with remote server
                val comments = localDatabaseService.getAllComments().toDto()
                val gate = Semaphore(5)

                val results: List<Boolean> = comments.map { commentDTO ->
                    async(Dispatchers.IO) {
                        gate.withPermit {
                            runCatching {
                                val path = "${DataConstant.NEWS_PATH}/${commentDTO.selectedNewId}/${DataConstant.COMMENT_PATH}"
                                databaseService.saveInstanceToDatabase(commentDTO.id, path, commentDTO)
                                true
                            }.getOrElse { false }
                        }
                    }
                }.awaitAll()

                val commentsOk = results.all { it }
                allOk = allOk && commentsOk
                clearComments()
            }

            allOk
        } catch(ex : Exception) {
            logMessage("syncData", { "Exception when sync data: ${ex.message}" })
            false
        }
    }

    override suspend fun clearLikedPosts() {
        localDatabaseService.clearLikedPosts()
    }

    override suspend fun clearComments() {
        localDatabaseService.clearComments()
    }

    override suspend fun loadNewsPostedWhenOffline(): List<NewsInstance> {
        return localDatabaseService.loadNewsPostedWhenOffline().toDomain()
    }

    override suspend fun deleteAllDraftPosts() {
        return localDatabaseService.deleteAllDraftPosts()
    }
}