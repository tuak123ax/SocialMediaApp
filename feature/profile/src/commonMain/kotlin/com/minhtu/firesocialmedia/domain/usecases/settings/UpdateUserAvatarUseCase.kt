package com.minhtu.firesocialmedia.domain.usecases.settings

import com.minhtu.firesocialmedia.constants.profile.DataConstant
import com.minhtu.firesocialmedia.data.remote.service.database.ProfileDatabaseService

class UpdateUserAvatarUseCase(private val databaseService: ProfileDatabaseService) {
    suspend operator fun invoke(userId: String, imageUri: String): Boolean {
        return databaseService.updateUserAvatar(userId, imageUri, DataConstant.USER_PATH)
    }
}
