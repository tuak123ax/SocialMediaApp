package com.minhtu.firesocialmedia.data.local.service.room

import com.minhtu.firesocialmedia.profile.data.remote.dto.user.UserDTO

/**
 * Room-backed local storage contract for User data.
 *
 * Feature-owned: feature/profile owns UserEntity/UserDao and every method here is typed against
 * core's [UserDTO] (not the Room UserEntity), so entity<->DTO conversion stays inside the platform
 * implementations living in feature/profile's androidMain/iosMain.
 *
 * feature/home also needs [clearLocalFriends] (HomeDbRepositoryImpl's logout flow) without a
 * Gradle dependency on feature/profile — that narrow need is served by feature/home's own
 * [com.minhtu.firesocialmedia.data.local.service.room.HomeUserRoomService], bridged to the same
 * underlying UserDao by appInit (which already depends on every feature).
 */
interface UserRoomService {
    suspend fun storeUserFriendsToRoom(friends: List<UserDTO?>)
    suspend fun storeUserFriendToRoom(friend: UserDTO)
    suspend fun getUserFromRoom(userId: String): UserDTO?
    suspend fun clearLocalFriends()
}
