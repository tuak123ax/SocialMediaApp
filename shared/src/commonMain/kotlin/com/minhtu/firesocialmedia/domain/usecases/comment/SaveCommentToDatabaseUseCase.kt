package com.minhtu.firesocialmedia.domain.usecases.comment

import com.minhtu.firesocialmedia.domain.entity.comment.CommentInstance
import com.minhtu.firesocialmedia.domain.repository.CommonDbRepository

class SaveCommentToDatabaseUseCase(
    private val commonDbRepository: CommonDbRepository
) {
    suspend operator fun invoke(
        selectedNewId: String,
        commentId : String,
        instance : CommentInstance) : Boolean {
        return commonDbRepository.saveCommentToDatabase(
            selectedNewId,
            commentId,
            instance
        )
    }
}