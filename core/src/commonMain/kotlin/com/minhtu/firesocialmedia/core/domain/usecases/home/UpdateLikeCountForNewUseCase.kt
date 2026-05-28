package com.minhtu.firesocialmedia.core.domain.usecases.home

import com.minhtu.firesocialmedia.core.domain.repository.CommonDbRepository

class UpdateLikeCountForNewUseCase(
    private val commonDbRepository: CommonDbRepository
) {
    suspend operator fun invoke(
        id: String,
        value: Int
    ) {
        commonDbRepository.updateLikeCountForNewInDatabase(id, value)
    }
}