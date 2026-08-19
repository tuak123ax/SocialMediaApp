package com.minhtu.firesocialmedia.domain.usecases.settings

import com.minhtu.firesocialmedia.domain.entity.settings.ChangePasswordState
import com.minhtu.firesocialmedia.security.entity.user.UserInstance
import com.minhtu.firesocialmedia.domain.repository.SecuritySettingsRepository

class ChangePasswordUseCase(
    private val settingsRepository: SecuritySettingsRepository
) {
    suspend operator fun invoke(user : UserInstance,
                                newPassword : String) : ChangePasswordState{
        return settingsRepository.changePassword(user, newPassword)
    }
}
