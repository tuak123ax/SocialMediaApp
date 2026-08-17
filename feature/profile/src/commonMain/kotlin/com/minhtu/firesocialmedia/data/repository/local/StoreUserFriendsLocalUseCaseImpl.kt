package com.minhtu.firesocialmedia.data.repository.local

import com.minhtu.firesocialmedia.profile.data.remote.dto.user.UserDTO
import com.minhtu.firesocialmedia.data.local.service.room.UserRoomService

class StoreUserFriendsLocalUseCaseImpl(
    private val userRoomService: UserRoomService
) {
    suspend operator fun invoke(friends: List<UserDTO?>) {
        userRoomService.storeUserFriendsToRoom(friends)
    }
}
