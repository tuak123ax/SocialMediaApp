package com.minhtu.firesocialmedia.data.repository

import com.minhtu.firesocialmedia.constants.comment.DataConstant
import com.minhtu.firesocialmedia.data.remote.mapper.comment.toDomain
import com.minhtu.firesocialmedia.data.remote.service.database.CommentDatabaseService
import com.minhtu.firesocialmedia.domain.entity.comment.CommentInstance
import com.minhtu.firesocialmedia.domain.repository.CommentRepository

class CommentRepositoryImpl(
    private val databaseService: CommentDatabaseService
) : CommentRepository {
    override suspend fun getAllComments(
        newsId : String): List<CommentInstance> =
        databaseService.getAllComments(DataConstant.COMMENT_PATH, newsId)
            .orEmpty().map { it.toDomain() }
}