package com.minhtu.firesocialmedia.ios.service.serviceimpl.room

import com.minhtu.firesocialmedia.profile.data.remote.dto.user.UserDTO
import com.minhtu.firesocialmedia.data.local.service.room.UserRoomService

/**
 * No-op stub, matching the original (pre-Phase-3) `IosRoomService` convention: iOS Room access is
 * not wired up to a real database (see instruction.md).
 */
class IosUserRoomService : UserRoomService {
    override suspend fun storeUserFriendsToRoom(friends: List<UserDTO?>) {
        // no-op
    }

    override suspend fun storeUserFriendToRoom(friend: UserDTO) {
        // no-op
    }

    override suspend fun getUserFromRoom(userId: String): UserDTO? {
        return null
    }

    override suspend fun clearLocalFriends() {
        // no-op
    }
}
