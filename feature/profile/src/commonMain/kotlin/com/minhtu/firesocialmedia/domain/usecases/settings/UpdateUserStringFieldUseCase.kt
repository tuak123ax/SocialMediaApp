package com.minhtu.firesocialmedia.domain.usecases.settings

import com.minhtu.firesocialmedia.constants.profile.DataConstant
import com.minhtu.firesocialmedia.data.remote.service.database.ProfileDatabaseService

class UpdateUserStringFieldUseCase(
    private val databaseService: ProfileDatabaseService
) {
    suspend operator fun invoke(userId: String, fieldPath: String, value: String): Boolean {
        return databaseService.updateUserStringField(userId, fieldPath, value, DataConstant.USER_PATH)
    }
}
