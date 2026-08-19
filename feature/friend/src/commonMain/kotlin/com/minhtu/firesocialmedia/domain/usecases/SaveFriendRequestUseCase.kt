package com.minhtu.firesocialmedia.domain.usecases.friend

import com.minhtu.firesocialmedia.domain.repository.FriendDbRepository

class SaveFriendRequestUseCase(
    private val friendDbRepository: FriendDbRepository
) {
    suspend operator fun invoke(id : String,
                                value : ArrayList<String>) {
        friendDbRepository.saveFriendRequest(id, value)
    }
}
