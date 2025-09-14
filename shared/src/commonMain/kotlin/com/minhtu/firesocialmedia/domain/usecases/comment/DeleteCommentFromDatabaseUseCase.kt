package com.minhtu.firesocialmedia.domain.usecases.comment

import com.minhtu.firesocialmedia.domain.entity.base.BaseNewsInstance
import com.minhtu.firesocialmedia.domain.repository.CommonDbRepository

class DeleteCommentFromDatabaseUseCase(
    private val commonDbRepository: CommonDbRepository
) {
    suspend operator fun invoke(selectedNewId: String,
                                comment: BaseNewsInstance) {
        commonDbRepository.deleteCommentFromDatabase(selectedNewId, comment)
    }
}