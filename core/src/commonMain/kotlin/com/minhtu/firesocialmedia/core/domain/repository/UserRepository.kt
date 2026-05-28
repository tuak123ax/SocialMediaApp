package com.minhtu.firesocialmedia.core.domain.repository

import com.minhtu.firesocialmedia.core.domain.entity.user.UserInstance

interface UserRepository {
    suspend fun getUser(userId : String, isCurrentUser: Boolean) : UserInstance?
    suspend fun getCurrentUserUid() : String?
    suspend fun updateFCMTokenForCurrentUser(user : UserInstance)
    suspend fun searchUserByName(name: String): List<UserInstance>?
}