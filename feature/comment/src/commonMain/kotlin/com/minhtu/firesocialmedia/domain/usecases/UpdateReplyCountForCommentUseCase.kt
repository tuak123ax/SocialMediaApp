package com.minhtu.firesocialmedia.domain.usecases.comment

import com.minhtu.firesocialmedia.domain.repository.CommentDbRepository

class UpdateReplyCountForCommentUseCase(
    private val commentDbRepository: CommentDbRepository
) {
    suspend operator fun invoke(id: String,
                                currentCommentId : String,
                                value: Int) {
        commentDbRepository.updateReplyCountForCommentInDatabase(id, currentCommentId, value)
    }
}
