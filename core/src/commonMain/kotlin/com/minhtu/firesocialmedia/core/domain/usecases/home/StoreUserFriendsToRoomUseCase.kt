package com.minhtu.firesocialmedia.core.domain.usecases.home

import com.minhtu.firesocialmedia.core.domain.entity.user.UserInstance
import com.minhtu.firesocialmedia.core.domain.repository.LocalRepository

class StoreUserFriendsToRoomUseCase(
    private val localRepository: LocalRepository
) {
    suspend operator fun invoke(friends: List<UserInstance?>) {
        localRepository.storeUserFriendsToRoom(friends)
    }
}