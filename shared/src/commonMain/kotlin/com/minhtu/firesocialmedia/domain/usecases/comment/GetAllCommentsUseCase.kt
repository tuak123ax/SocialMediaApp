package com.minhtu.firesocialmedia.domain.usecases.comment

import com.minhtu.firesocialmedia.domain.entity.comment.CommentInstance
import com.minhtu.firesocialmedia.domain.repository.CommentRepository

class GetAllCommentsUseCase(
    private val commentRepository: CommentRepository
) {
    suspend operator fun invoke(path: String,
                                newsId: String) : List<CommentInstance> {
        return commentRepository.getAllComments(path, newsId)
    }
}