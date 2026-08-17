package com.minhtu.firesocialmedia.domain.repository

import com.minhtu.firesocialmedia.home.entity.comment.CommentInstance

/**
 * Feature-home-owned clone of feature/comment's `domain.repository.CommentRepository`.
 */
interface HomeCommentRepository {
    suspend fun getAllComments(newsId: String) : List<CommentInstance>
}
