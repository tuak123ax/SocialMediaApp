package com.minhtu.firesocialmedia.domain.repository

import com.minhtu.firesocialmedia.domain.entity.comment.CommentInstance

interface CommentRepository {
    suspend fun getAllComments(path: String,
                               newsId: String) : List<CommentInstance>
}