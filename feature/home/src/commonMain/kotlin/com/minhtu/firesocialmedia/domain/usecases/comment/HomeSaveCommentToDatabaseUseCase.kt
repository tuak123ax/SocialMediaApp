package com.minhtu.firesocialmedia.domain.usecases.comment

import com.minhtu.firesocialmedia.home.entity.comment.CommentInstance
import com.minhtu.firesocialmedia.domain.repository.HomeCommentDbRepository

class HomeSaveCommentToDatabaseUseCase(
    private val commentDbRepository: HomeCommentDbRepository
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
