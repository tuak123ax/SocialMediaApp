package com.minhtu.firesocialmedia.domain.interactor.home

import com.minhtu.firesocialmedia.domain.entity.user.UserInstance

interface UserInteractor {
    suspend fun getCurrentUserId(): String?
    suspend fun getUser(userId: String): UserInstance?
    suspend fun updateFcmToken(user: UserInstance)
    suspend fun clearLocalAccount()
    suspend fun saveLikedPost(id : String,
                              path : String,
                              value : HashMap<String, Int>,
                              externalPath : String) : Boolean
    suspend fun searchUserByName(name: String,
                                 path: String): List<UserInstance>?
}