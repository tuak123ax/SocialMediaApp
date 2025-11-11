package com.minhtu.firesocialmedia.application.interactor

import com.minhtu.firesocialmedia.domain.entity.user.UserInstance
import com.minhtu.firesocialmedia.domain.interactor.home.UserInteractor
import com.minhtu.firesocialmedia.domain.usecases.common.GetCurrentUserUidUseCase
import com.minhtu.firesocialmedia.domain.usecases.common.GetUserUseCase
import com.minhtu.firesocialmedia.domain.usecases.home.ClearAccountUseCase
import com.minhtu.firesocialmedia.domain.usecases.home.ClearLocalDataUseCase
import com.minhtu.firesocialmedia.domain.usecases.home.SaveCurrentUserInfoUseCase
import com.minhtu.firesocialmedia.domain.usecases.home.SaveLikedPostUseCase
import com.minhtu.firesocialmedia.domain.usecases.home.SearchUserByNameUseCase
import com.minhtu.firesocialmedia.domain.usecases.home.StoreUserFriendsToRoomUseCase
import com.minhtu.firesocialmedia.domain.usecases.home.UpdateFCMTokenUseCase

class UserInteractorImpl(
    private val getCurrentUserUidUseCase : GetCurrentUserUidUseCase,
    private val getUserUseCase: GetUserUseCase,
    private val updateFCMTokenUseCase: UpdateFCMTokenUseCase,
    private val clearAccountUseCase: ClearAccountUseCase,
    private val saveLikedPostUseCase: SaveLikedPostUseCase,
    private val searchUserByNameUseCase: SearchUserByNameUseCase,
    private val storeUserFriendsToRoomUseCase : StoreUserFriendsToRoomUseCase,
    private val saveCurrentUserInfoUseCase : SaveCurrentUserInfoUseCase,
    private val clearLocalDataUseCase: ClearLocalDataUseCase
) : UserInteractor {
    override suspend fun getCurrentUserId(): String? {
        return getCurrentUserUidUseCase.invoke()
    }

    override suspend fun getUser(
        userId: String,
        isCurrentUser: Boolean
    ): UserInstance? {
        return getUserUseCase.invoke(userId, isCurrentUser)
    }

    override suspend fun updateFcmToken(user: UserInstance) {
        updateFCMTokenUseCase.invoke(user)
    }

    override suspend fun saveCurrentUserInfo(user: UserInstance) {
        saveCurrentUserInfoUseCase.invoke(user)
    }

    override suspend fun clearLocalAccount() {
        clearAccountUseCase.invoke()
    }

    override suspend fun saveLikedPost(id : String,
                                       value : HashMap<String, Int>) : Boolean {
        return saveLikedPostUseCase.invoke(
            id,
            value
        )
    }

    override suspend fun searchUserByName(name: String): List<UserInstance>? {
        return searchUserByNameUseCase.invoke(name)
    }

    override suspend fun storeUserFriendsToRoom(friends: List<UserInstance?>) {
        storeUserFriendsToRoomUseCase.invoke(friends)
    }

    override suspend fun clearLocalData() {
        clearLocalDataUseCase.invoke()
    }
}