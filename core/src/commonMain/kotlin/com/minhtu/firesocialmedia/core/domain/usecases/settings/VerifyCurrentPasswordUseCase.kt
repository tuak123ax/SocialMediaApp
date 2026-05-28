package com.minhtu.firesocialmedia.core.domain.usecases.settings

import com.minhtu.firesocialmedia.core.domain.repository.SettingsRepository

class VerifyCurrentPasswordUseCase(
    private val settingsRepository: SettingsRepository
) {
    suspend operator fun invoke(currentUserEmail : String,
                                currentPassword : String) : Boolean {
        return settingsRepository.reAuthenticate(currentUserEmail, currentPassword)
    }
}