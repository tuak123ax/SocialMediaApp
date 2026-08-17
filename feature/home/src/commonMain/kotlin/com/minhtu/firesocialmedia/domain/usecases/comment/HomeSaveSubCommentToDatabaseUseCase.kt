package com.minhtu.firesocialmedia.domain.usecases.comment

import com.minhtu.firesocialmedia.home.entity.comment.base.BaseNewsInstance
import com.minhtu.firesocialmedia.domain.repository.HomeCommentDbRepository

class HomeSaveSubCommentToDatabaseUseCase(
    private val commentDbRepository: HomeCommentDbRepository
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
