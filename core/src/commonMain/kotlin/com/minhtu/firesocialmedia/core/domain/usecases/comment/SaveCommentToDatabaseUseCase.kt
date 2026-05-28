package com.minhtu.firesocialmedia.core.domain.usecases.comment

import com.minhtu.firesocialmedia.core.domain.entity.comment.CommentInstance
import com.minhtu.firesocialmedia.core.domain.repository.CommonDbRepository

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