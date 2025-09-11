package com.minhtu.firesocialmedia.application.interactor

import com.minhtu.firesocialmedia.constants.Constants
import com.minhtu.firesocialmedia.domain.entity.user.UserInstance
import com.minhtu.firesocialmedia.domain.interactor.home.UserInteractor
import com.minhtu.firesocialmedia.domain.usecases.common.GetUserUseCase
import com.minhtu.firesocialmedia.domain.usecases.common.SaveValueToDatabaseUseCase
import com.minhtu.firesocialmedia.domain.usecases.home.ClearAccountUseCase
import com.minhtu.firesocialmedia.domain.usecases.common.GetCurrentUserUidUseCase
import com.minhtu.firesocialmedia.domain.usecases.home.SearchUserByNameUseCase
import com.minhtu.firesocialmedia.domain.usecases.home.UpdateFCMTokenUseCase

class UserInteractorImpl(
    private val getCurrentUserUidUseCase : GetCurrentUserUidUseCase,
    private val getUserUseCase: GetUserUseCase,
    private val updateFCMTokenUseCase: UpdateFCMTokenUseCase,
    private val clearAccountUseCase: ClearAccountUseCase,
    private val saveValueToDatabaseUseCase: SaveValueToDatabaseUseCase,
    private val searchUserByNameUseCase: SearchUserByNameUseCase
) : UserInteractor {
    override suspend fun getCurrentUserId(): String? {
        return getCurrentUserUidUseCase.invoke()
    }

    override suspend fun getUser(userId: String): UserInstance? {
        return getUserUseCase.invoke(userId)
    }

    override suspend fun updateFcmToken(user: UserInstance) {
        updateFCMTokenUseCase.invoke(user)
    }

    override suspend fun clearLocalAccount() {
        clearAccountUseCase.invoke()
    }

    override suspend fun saveLikedPost(id : String,
                                       path : String,
                                       value : HashMap<String, Int>,
                                       externalPath : String) : Boolean {
        return saveValueToDatabaseUseCase.invoke(
            id,
            path,
            value,
            externalPath
        )
    }

    override suspend fun searchUserByName(name: String,
                                          path: String): List<UserInstance>? {
        return searchUserByNameUseCase.invoke(name, Constants.USER_PATH)
    }
}