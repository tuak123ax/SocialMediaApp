package com.minhtu.firesocialmedia.domain.repository

interface ProfileFriendDbRepository {
    suspend fun saveFriend(id: String, value: ArrayList<String>)
    suspend fun saveFriendRequest(id: String, value: ArrayList<String>)
}
