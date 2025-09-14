package com.minhtu.firesocialmedia.domain.usecases.comment

import com.minhtu.firesocialmedia.domain.entity.base.BaseNewsInstance
import com.minhtu.firesocialmedia.domain.repository.CommonDbRepository

class SaveCommentToDatabaseUseCase(
    private val commonDbRepository: CommonDbRepository
) {
    suspend operator fun invoke(
        id : String,
        instance : BaseNewsInstance) : Boolean {
        return commonDbRepository.saveCommentToDatabase(
            id,
            instance
        )
    }
}