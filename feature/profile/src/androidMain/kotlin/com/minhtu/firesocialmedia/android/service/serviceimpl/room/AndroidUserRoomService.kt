package com.minhtu.firesocialmedia.android.service.serviceimpl.room

import com.minhtu.firesocialmedia.profile.data.remote.dto.user.UserDTO
import com.minhtu.firesocialmedia.data.local.dao.UserDao
import com.minhtu.firesocialmedia.data.local.mapper.room.toDomain
import com.minhtu.firesocialmedia.data.local.mapper.room.toRoomEntity
import com.minhtu.firesocialmedia.data.local.service.room.UserRoomService

class AndroidUserRoomService(
    private val userDao: UserDao
) : UserRoomService {
    override suspend fun storeUserFriendsToRoom(friends: List<UserDTO?>) {
        val entities = friends.mapNotNull { it?.toRoomEntity() }
        if (entities.isNotEmpty()) {
            userDao.addAll(entities)
        }
    }

    override suspend fun storeUserFriendToRoom(friend: UserDTO) {
        userDao.add(friend.toRoomEntity())
    }

    override suspend fun getUserFromRoom(userId: String): UserDTO? {
        return userDao.getById(userId)?.toDomain()
    }

    override suspend fun clearLocalFriends() {
        userDao.clearUserFriends()
    }
}
