package com.minhtu.firesocialmedia.domain.usecases.home

import com.minhtu.firesocialmedia.domain.repository.HomeDbRepository

class UpdateLikeCountForNewUseCase(
    private val homeDbRepository: HomeDbRepository
) {
    suspend operator fun invoke(
        id: String,
        value: Int
    ) {
        homeDbRepository.updateLikeCountForNewInDatabase(id, value)
    }
}
