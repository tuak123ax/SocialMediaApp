package com.minhtu.firesocialmedia.domain.usecases.friend

import com.minhtu.firesocialmedia.domain.repository.ProfileFriendDbRepository

class ProfileSaveFriendRequestUseCase(
    private val friendDbRepository: ProfileFriendDbRepository
) {
    suspend operator fun invoke(id: String, value: ArrayList<String>) {
        friendDbRepository.saveFriendRequest(id, value)
    }
}
