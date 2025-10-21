package com.minhtu.firesocialmedia.domain.usecases.home

import com.minhtu.firesocialmedia.domain.repository.CommonDbRepository

class ClearLocalDataUseCase(
    private val commonDbRepository: CommonDbRepository
) {
    suspend operator fun invoke() {
        commonDbRepository.clearLikedPosts()
        commonDbRepository.clearComments()
    }
}