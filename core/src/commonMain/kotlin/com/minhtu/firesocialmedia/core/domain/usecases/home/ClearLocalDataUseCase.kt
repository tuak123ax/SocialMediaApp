package com.minhtu.firesocialmedia.core.domain.usecases.home

import com.minhtu.firesocialmedia.core.domain.repository.CommonDbRepository

class ClearLocalDataUseCase(
    private val commonDbRepository: CommonDbRepository
) {
    suspend operator fun invoke() {
        commonDbRepository.clearLikedPosts()
        commonDbRepository.clearComments()
    }
}