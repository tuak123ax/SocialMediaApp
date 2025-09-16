package com.minhtu.firesocialmedia.data.repository

import com.minhtu.firesocialmedia.data.constant.DataConstant
import com.minhtu.firesocialmedia.data.mapper.comment.toDomain
import com.minhtu.firesocialmedia.domain.entity.comment.CommentInstance
import com.minhtu.firesocialmedia.domain.repository.CommentRepository
import com.minhtu.firesocialmedia.data.remote.service.database.DatabaseService

class CommentRepositoryImpl(
    private val databaseService: DatabaseService
) : CommentRepository {
    override suspend fun getAllComments(
        newId : String): List<CommentInstance> =
        databaseService.getAllComments(DataConstant.COMMENT_PATH, newId)
            .orEmpty().map { it.toDomain() }
}