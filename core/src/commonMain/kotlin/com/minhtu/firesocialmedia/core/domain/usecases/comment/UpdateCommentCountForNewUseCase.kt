package com.minhtu.firesocialmedia.core.domain.usecases.comment

import com.minhtu.firesocialmedia.core.domain.repository.CommonDbRepository

class UpdateCommentCountForNewUseCase(
    private val commonDbRepository: CommonDbRepository
) {
    suspend operator fun invoke(id: String,
                                value: Int) {
        commonDbRepository.updateCommentCountForNewInDatabase(id, value)
    }
}