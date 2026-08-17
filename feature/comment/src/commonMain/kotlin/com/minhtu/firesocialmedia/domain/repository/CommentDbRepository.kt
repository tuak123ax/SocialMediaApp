package com.minhtu.firesocialmedia.domain.repository

import com.minhtu.firesocialmedia.domain.entity.base.BaseNewsInstance
import com.minhtu.firesocialmedia.domain.entity.comment.CommentInstance

interface CommentDbRepository {
    suspend fun saveCommentToDatabase(
        selectedNewId: String,
        commentId: String,
        instance: CommentInstance
    ): Boolean

    suspend fun saveSubCommentToDatabase(
        id: String,
        selectedNewId: String,
        parentCommentId: String,
        instance: BaseNewsInstance
    ): Boolean

    suspend fun deleteCommentFromDatabase(
        selectedNewId: String,
        instance: BaseNewsInstance
    )

    suspend fun deleteSubCommentFromDatabase(
        selectedNewId: String,
        parentCommentId: String,
        instance: BaseNewsInstance
    )

    suspend fun updateCommentCountForNewInDatabase(
        id: String,
        value: Int
    )

    suspend fun updateReplyCountForCommentInDatabase(
        id: String,
        currentCommentId: String,
        value: Int
    )

    suspend fun updateLikeCountForCommentInDatabase(
        selectedNewId: String,
        likedComment: String,
        value: Int
    )

    suspend fun updateLikeCountForSubCommentInDatabase(
        selectedNewId: String,
        likedComment: String,
        parentCommentId: String,
        value: Int
    )

    suspend fun saveLikedComments(id: String, map: HashMap<String, Int>): Boolean

    suspend fun syncComments(): Boolean
    suspend fun clearComments()
}
