package com.minhtu.firesocialmedia.domain.usecases.friend

import com.minhtu.firesocialmedia.domain.repository.FriendDbRepository

class SaveFriendUseCase(
    private val friendDbRepository: FriendDbRepository
) {
    suspend operator fun invoke(id : String,
                                value : ArrayList<String>) {
        friendDbRepository.saveFriend(id ,value)
    }
}
