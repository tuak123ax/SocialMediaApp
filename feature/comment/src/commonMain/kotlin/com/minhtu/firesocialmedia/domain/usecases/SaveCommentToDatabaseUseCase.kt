package com.minhtu.firesocialmedia.domain.usecases.comment

import com.minhtu.firesocialmedia.domain.entity.comment.CommentInstance
import com.minhtu.firesocialmedia.domain.repository.CommentDbRepository

class SaveCommentToDatabaseUseCase(
    private val commentDbRepository: CommentDbRepository
) {
    suspend operator fun invoke(
        selectedNewId: String,
        commentId : String,
        instance : CommentInstance) : Boolean {
        return commentDbRepository.saveCommentToDatabase(
            selectedNewId,
            commentId,
            instance
        )
    }
}
