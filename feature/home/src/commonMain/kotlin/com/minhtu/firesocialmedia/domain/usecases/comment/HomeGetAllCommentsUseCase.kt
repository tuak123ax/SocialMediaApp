package com.minhtu.firesocialmedia.domain.usecases.comment

import com.minhtu.firesocialmedia.home.entity.comment.CommentInstance
import com.minhtu.firesocialmedia.domain.repository.HomeCommentRepository

class HomeGetAllCommentsUseCase(
    private val commentRepository: HomeCommentRepository
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
