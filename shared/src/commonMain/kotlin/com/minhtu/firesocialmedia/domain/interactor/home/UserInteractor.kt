package com.minhtu.firesocialmedia.domain.interactor.home

import com.minhtu.firesocialmedia.domain.entity.user.UserInstance

interface UserInteractor {
    suspend fun getCurrentUserId(): String?
    suspend fun getUser(userId: String, isCurrentUser : Boolean): UserInstance?
    suspend fun updateFcmToken(user: UserInstance)
    suspend fun saveCurrentUserInfo(user : UserInstance)
    suspend fun clearLocalAccount()
    suspend fun saveLikedPost(id : String,
                              value : HashMap<String, Int>) : Boolean
    suspend fun searchUserByName(name: String): List<UserInstance>?
    suspend fun storeUserFriendsToRoom(friends : List<UserInstance?>)
    suspend fun clearLocalData()
}