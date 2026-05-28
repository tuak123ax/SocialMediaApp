package com.minhtu.firesocialmedia.core.domain.usecases.comment

import com.minhtu.firesocialmedia.core.domain.entity.comment.CommentInstance
import com.minhtu.firesocialmedia.core.domain.repository.CommentRepository

class GetAllCommentsUseCase(
    private val commentRepository: CommentRepository
) {
    suspend operator fun invoke(newsId: String): List<CommentInstance> {
        val result = commentRepository.getAllComments(newsId)
        return result.filter { comment ->
            comment.message.isNotBlank() ||
                    comment.image.isNotBlank() ||
                    comment.video.isNotBlank()
        }
    }
}