package com.minhtu.firesocialmedia.data.repository

import com.minhtu.firesocialmedia.data.mapper.comment.toDomain
import com.minhtu.firesocialmedia.domain.entity.comment.CommentInstance
import com.minhtu.firesocialmedia.domain.repository.CommentRepository
import com.minhtu.firesocialmedia.domain.service.database.DatabaseService

class CommentRepositoryImpl(
    private val databaseService: DatabaseService
) : CommentRepository {
    override suspend fun getAllComments(
        path: String,
        newId : String): List<CommentInstance> =
        databaseService.getAllComments(path, newId)
            ?.map { it.toDomain() }
            .orEmpty()
}