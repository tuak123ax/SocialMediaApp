package com.minhtu.firesocialmedia.core.domain.usecases.settings

import com.minhtu.firesocialmedia.core.domain.entity.settings.ChangePasswordState
import com.minhtu.firesocialmedia.core.domain.entity.user.UserInstance
import com.minhtu.firesocialmedia.core.domain.repository.SettingsRepository

class ChangePasswordUseCase(
    private val settingsRepository: SettingsRepository
) {
    suspend operator fun invoke(user : UserInstance,
                                newPassword : String) : ChangePasswordState{
        return settingsRepository.changePassword(user, newPassword)
    }
}