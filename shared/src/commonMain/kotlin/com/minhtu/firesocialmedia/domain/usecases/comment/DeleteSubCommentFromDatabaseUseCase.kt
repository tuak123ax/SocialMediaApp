package com.minhtu.firesocialmedia.domain.usecases.comment

import com.minhtu.firesocialmedia.domain.entity.base.BaseNewsInstance
import com.minhtu.firesocialmedia.domain.repository.CommonDbRepository

class DeleteSubCommentFromDatabaseUseCase(
    private val commonDbRepository: CommonDbRepository
) {
    suspend operator fun invoke(selectedNewId: String,
                                parentCommentId : String,
                                comment: BaseNewsInstance) {
        commonDbRepository.deleteSubCommentFromDatabase(selectedNewId, parentCommentId, comment)
    }
}