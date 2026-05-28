package com.minhtu.firesocialmedia.core.domain.repository

import com.minhtu.firesocialmedia.core.domain.entity.comment.CommentInstance

interface CommentRepository {
    suspend fun getAllComments(newsId: String) : List<CommentInstance>
}