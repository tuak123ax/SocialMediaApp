package com.minhtu.firesocialmedia.domain.repository

import com.minhtu.firesocialmedia.domain.entity.user.UserInstance

interface UserRepository {
    suspend fun getUser(userId : String) : UserInstance?
    suspend fun getCurrentUserUid() : String?
    suspend fun updateFCMTokenForCurrentUser(user : UserInstance)
    suspend fun searchUserByName(name: String,
                                 path: String): List<UserInstance>?
}