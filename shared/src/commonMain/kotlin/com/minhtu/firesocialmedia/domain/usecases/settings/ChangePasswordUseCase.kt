package com.minhtu.firesocialmedia.domain.usecases.settings

import com.minhtu.firesocialmedia.domain.entity.settings.ChangePasswordState
import com.minhtu.firesocialmedia.domain.entity.user.UserInstance
import com.minhtu.firesocialmedia.domain.repository.SettingsRepository

class ChangePasswordUseCase(
    private val settingsRepository: SettingsRepository
) {
    suspend operator fun invoke(user : UserInstance,
                                newPassword : String) : ChangePasswordState{
        return settingsRepository.changePassword(user, newPassword)
    }
}