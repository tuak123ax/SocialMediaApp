package com.minhtu.firesocialmedia.domain.usecases.home

import com.minhtu.firesocialmedia.home.entity.user.UserInstance
import com.minhtu.firesocialmedia.home.entity.user.toDto

class StoreUserFriendsToRoomUseCase(
    private val friendsLocalStore: FriendsLocalStore
) {
    suspend operator fun invoke(friends: List<UserInstance?>) {
        friendsLocalStore.store(friends.map { it?.toDto() })
    }
}
