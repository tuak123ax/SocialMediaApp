package com.minhtu.firesocialmedia.domain.usecases.comment

import com.minhtu.firesocialmedia.domain.entity.comment.CommentInstance
import com.minhtu.firesocialmedia.domain.repository.CommentRepository

class GetAllCommentsUseCase(
    private val commentRepository: CommentRepository
) {
    suspend operator fun invoke(newsId: String) : List<CommentInstance> {
        return commentRepository.getAllComments(newsId)
    }
}