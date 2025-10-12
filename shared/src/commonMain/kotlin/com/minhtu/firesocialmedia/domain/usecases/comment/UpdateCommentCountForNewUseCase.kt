package com.minhtu.firesocialmedia.domain.usecases.comment

import com.minhtu.firesocialmedia.domain.repository.CommonDbRepository

class UpdateCommentCountForNewUseCase(
    private val commonDbRepository: CommonDbRepository
) {
    suspend operator fun invoke(id: String,
                                value: Int) {
        commonDbRepository.updateCommentCountForNewInDatabase(id, value)
    }
}