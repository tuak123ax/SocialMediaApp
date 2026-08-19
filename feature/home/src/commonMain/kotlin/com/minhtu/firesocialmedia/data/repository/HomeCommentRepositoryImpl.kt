package com.minhtu.firesocialmedia.data.repository

import com.minhtu.firesocialmedia.constants.home.DataConstant
import com.minhtu.firesocialmedia.data.remote.mapper.comment.toDomain
import com.minhtu.firesocialmedia.data.remote.service.database.HomeCommentDatabaseService
import com.minhtu.firesocialmedia.home.entity.comment.CommentInstance
import com.minhtu.firesocialmedia.domain.repository.HomeCommentRepository

class HomeCommentRepositoryImpl(
    private val databaseService: HomeCommentDatabaseService
) : HomeCommentRepository {
    override suspend fun getAllComments(
        newsId : String): List<CommentInstance> =
        databaseService.getAllComments(DataConstant.COMMENT_PATH, newsId)
            .orEmpty().map { it.toDomain() }
}
