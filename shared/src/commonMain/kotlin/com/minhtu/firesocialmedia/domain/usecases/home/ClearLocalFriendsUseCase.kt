package com.minhtu.firesocialmedia.domain.usecases.home

import com.minhtu.firesocialmedia.domain.repository.CommonDbRepository

class ClearLocalFriendsUseCase(
    private val commonDbRepository: CommonDbRepository
) {
    suspend operator fun invoke() {
        commonDbRepository.clearLocalFriends()
    }
}