package com.minhtu.firesocialmedia.data.local.service.room

/**
 * Room-backed local storage contract for the narrow User-room need feature/home has: clearing
 * locally cached friends on logout (HomeDbRepositoryImpl.clearLocalFriends).
 *
 * feature/home has no Gradle dependency on feature/profile (which owns UserEntity/UserDao via its
 * own [com.minhtu.firesocialmedia.data.local.service.room.UserRoomService]), so this is a local
 * clone interface rather than a shared/imported type. appInit — which already depends on every
 * feature — supplies the concrete implementation, delegating to the same underlying UserDao
 * feature/profile writes to, so there's no data duplication.
 */
fun interface HomeUserRoomService {
    suspend fun clearLocalFriends()
}
