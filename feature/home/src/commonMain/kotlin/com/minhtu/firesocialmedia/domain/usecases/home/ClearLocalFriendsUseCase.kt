package com.minhtu.firesocialmedia.domain.usecases.home

import com.minhtu.firesocialmedia.domain.repository.HomeDbRepository

class ClearLocalFriendsUseCase(
    private val homeDbRepository: HomeDbRepository
) {
    suspend operator fun invoke() {
        homeDbRepository.clearLocalFriends()
    }
}
