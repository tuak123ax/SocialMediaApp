package com.minhtu.firesocialmedia.data.repository

import com.minhtu.firesocialmedia.data.remote.constant.DataConstant
import com.minhtu.firesocialmedia.data.remote.mapper.user.toDto
import com.minhtu.firesocialmedia.data.remote.service.auth.AuthService
import com.minhtu.firesocialmedia.domain.entity.settings.ChangePasswordState
import com.minhtu.firesocialmedia.domain.entity.user.UserInstance
import com.minhtu.firesocialmedia.domain.repository.SettingsRepository

class SettingsRepositoryImpl(
    private val authService: AuthService
) : SettingsRepository {
    override suspend fun changePassword(user : UserInstance,
                                        newPassword: String): ChangePasswordState {
        return authService.changePassword(
            user.toDto(),
            newPassword,
            DataConstant.USER_PATH,
            DataConstant.LAST_TIME_CHANGE_PASSWORD_PATH)
    }

    override suspend fun reAuthenticate(
        currentUserEmail: String,
        currentPassword: String
    ) : Boolean {
        return authService.reAuthenticate(currentUserEmail, currentPassword)
    }
}