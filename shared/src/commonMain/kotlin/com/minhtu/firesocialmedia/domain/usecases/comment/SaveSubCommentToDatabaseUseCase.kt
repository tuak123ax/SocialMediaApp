package com.minhtu.firesocialmedia.domain.usecases.comment

import com.minhtu.firesocialmedia.domain.entity.base.BaseNewsInstance
import com.minhtu.firesocialmedia.domain.repository.CommonDbRepository

class SaveSubCommentToDatabaseUseCase(
    private val commonDbRepository: CommonDbRepository
) {
    suspend operator fun invoke(
        id : String,
        selectedNewId : String,
        parentCommentId : String,
        instance : BaseNewsInstance) : Boolean {
        return commonDbRepository.saveSubCommentToDatabase(
            id,
            selectedNewId,
            parentCommentId,
            instance
        )
    }
}