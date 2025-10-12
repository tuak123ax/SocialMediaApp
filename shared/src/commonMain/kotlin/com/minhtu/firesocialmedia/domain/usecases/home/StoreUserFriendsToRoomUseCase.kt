package com.minhtu.firesocialmedia.domain.usecases.home

import com.minhtu.firesocialmedia.domain.entity.user.UserInstance
import com.minhtu.firesocialmedia.domain.repository.LocalRepository

class StoreUserFriendsToRoomUseCase(
    private val localRepository: LocalRepository
) {
    suspend operator fun invoke(friends: List<UserInstance?>) {
        localRepository.storeUserFriendsToRoom(friends)
    }
}