package com.minhtu.firesocialmedia.domain.usecases.home

import com.minhtu.firesocialmedia.domain.repository.CommonDbRepository

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