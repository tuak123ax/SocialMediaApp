package com.minhtu.firesocialmedia.domain.usecases.comment

import com.minhtu.firesocialmedia.domain.entity.base.BaseNewsInstance
import com.minhtu.firesocialmedia.domain.repository.CommentDbRepository

class SaveSubCommentToDatabaseUseCase(
    private val commentDbRepository: CommentDbRepository
) {
    suspend operator fun invoke(
        id : String,
        selectedNewId : String,
        parentCommentId : String,
        instance : BaseNewsInstance) : Boolean {
        return commentDbRepository.saveSubCommentToDatabase(
            id,
            selectedNewId,
            parentCommentId,
            instance
        )
    }
}
