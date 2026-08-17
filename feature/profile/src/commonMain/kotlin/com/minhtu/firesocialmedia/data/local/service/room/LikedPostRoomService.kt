package com.minhtu.firesocialmedia.data.local.service.room

/**
 * Room-backed local storage contract for the LikedPost primitive map, owned by feature/profile.
 *
 * feature/profile has no Gradle dependency on feature/home (which owns the underlying
 * NewsDao-backed LikedPost table via its own
 * [com.minhtu.firesocialmedia.data.local.service.room.HomeNewsRoomService]), so this is a local
 * clone interface rather than a shared/imported type. appInit — which already depends on every
 * feature — supplies the concrete implementation, delegating to the same underlying storage
 * feature/home writes to, so there's no data duplication.
 */
interface LikedPostRoomService {
    suspend fun saveLikedPost(value: HashMap<String, Int>)
    suspend fun getAllLikedPosts(): HashMap<String, Int>
    suspend fun clearLikedPosts()
    suspend fun hasLikedPost(): Boolean
}
