package com.minhtu.firesocialmedia.data.repository

import com.minhtu.firesocialmedia.data.local.mapper.comment.toDto
import com.minhtu.firesocialmedia.data.local.mapper.comment.toRoomRecord
import com.minhtu.firesocialmedia.data.local.service.room.HomeCommentRoomService
import com.minhtu.firesocialmedia.constants.home.DataConstant
import com.minhtu.firesocialmedia.data.remote.mapper.comment.toDomain
import com.minhtu.firesocialmedia.data.remote.service.database.HomeCommentStorageService
import com.minhtu.firesocialmedia.data.remote.service.database.HomeCommentDatabaseService
import com.minhtu.firesocialmedia.network.home.NetworkMonitor
import com.minhtu.firesocialmedia.home.entity.comment.base.BaseNewsInstance
import com.minhtu.firesocialmedia.home.entity.comment.CommentInstance
import com.minhtu.firesocialmedia.domain.repository.HomeCommentDbRepository
import com.minhtu.firesocialmedia.platform.logMessage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.supervisorScope
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withPermit

class HomeCommentDbRepositoryImpl(
    private val databaseService: HomeCommentDatabaseService,
    private val commentStorageService: HomeCommentStorageService,
    private val localDatabaseService: HomeCommentRoomService,
    private val networkMonitor: NetworkMonitor
) : HomeCommentDbRepository {
    override suspend fun saveCommentToDatabase(
        selectedNewId: String,
        commentId: String,
        instance: CommentInstance
    ): Boolean {
        val isOnline = networkMonitor.isOnline.first()
        if (isOnline) {
            return commentStorageService.saveInstanceToDatabase(
                commentId,
                DataConstant.NEWS_PATH + "/" + selectedNewId + "/" + DataConstant.COMMENT_PATH,
                instance
            )
        } else {
            try {
                localDatabaseService.saveComment(
                    instance.toRoomRecord(selectedNewId)
                )
                return true
            } catch (ex: Exception) {
                logMessage(
                    "saveCommentToDatabase",
                    { "Exception when saveCommentToDatabase: ${ex.message}" })
                return false
            }
        }
    }

    override suspend fun saveSubCommentToDatabase(
        id: String,
        selectedNewId: String,
        parentCommentId: String,
        instance: BaseNewsInstance
    ): Boolean {
        return commentStorageService.saveInstanceToDatabase(
            id,
            DataConstant.NEWS_PATH + "/" + selectedNewId + "/" + DataConstant.COMMENT_PATH + "/" + parentCommentId + "/" + DataConstant.LIST_REPLIES_PATH,
            instance
        )
    }

    override suspend fun deleteCommentFromDatabase(
        selectedNewId: String,
        instance: BaseNewsInstance
    ) {
        commentStorageService.deleteCommentFromDatabase(
            DataConstant.NEWS_PATH + "/" +
                    selectedNewId + "/" +
                    DataConstant.COMMENT_PATH,
            instance
        )
    }

    override suspend fun deleteSubCommentFromDatabase(
        selectedNewId: String,
        parentCommentId: String,
        instance: BaseNewsInstance
    ) {
        commentStorageService.deleteCommentFromDatabase(
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
        currentCommentId: String,
        value: Int
    ) {
        databaseService.updateCountValueInDatabase(
            id,
            DataConstant.NEWS_PATH,
            DataConstant.COMMENT_PATH + "/" + currentCommentId + "/"
                    + DataConstant.COMMENT_COUNT_PATH,
            value
        )
    }

    override suspend fun updateLikeCountForCommentInDatabase(
        selectedNewId: String,
        likedComment: String,
        value: Int
    ) {
        databaseService.updateCountValueInDatabase(
            selectedNewId,
            DataConstant.NEWS_PATH,
            DataConstant.COMMENT_PATH + "/" + likedComment + "/" + DataConstant.LIKED_COUNT_PATH,
            value
        )
    }

    override suspend fun updateLikeCountForSubCommentInDatabase(
        selectedNewId: String,
        likedComment: String,
        parentCommentId: String,
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
        map: HashMap<String, Int>
    ): Boolean {
        return databaseService.saveValueToDatabase(
            id,
            DataConstant.USER_PATH,
            map,
            DataConstant.LIKED_COMMENT_PATH
        )
    }

    override suspend fun syncComments(): Boolean = supervisorScope {
        if (!localDatabaseService.hasComment()) {
            return@supervisorScope true
        }
        try {
            val comments = localDatabaseService.getAllComments().toDto()
            val gate = Semaphore(5)

            val results: List<Boolean> = comments.map { commentDTO ->
                async(Dispatchers.IO) {
                    gate.withPermit {
                        runCatching {
                            val path =
                                "${DataConstant.NEWS_PATH}/${commentDTO.selectedNewId}/${DataConstant.COMMENT_PATH}"
                            commentStorageService.saveInstanceToDatabase(
                                commentDTO.id,
                                path,
                                commentDTO.toDomain()
                            )
                            true
                        }.getOrElse { false }
                    }
                }
            }.awaitAll()

            val commentsOk = results.all { it }
            clearComments()
            commentsOk
        } catch (ex: Exception) {
            logMessage("syncComments", { "Exception when sync comments: ${ex.message}" })
            false
        }
    }

    override suspend fun clearComments() {
        localDatabaseService.clearComments()
    }
}
