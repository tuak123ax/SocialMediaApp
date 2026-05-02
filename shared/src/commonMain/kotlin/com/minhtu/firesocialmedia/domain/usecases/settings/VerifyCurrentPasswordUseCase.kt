package com.minhtu.firesocialmedia.domain.usecases.settings

import com.minhtu.firesocialmedia.domain.repository.SettingsRepository

class VerifyCurrentPasswordUseCase(
    private val settingsRepository: SettingsRepository
) {
    suspend operator fun invoke(currentUserEmail : String,
                                currentPassword : String) : Boolean {
        return settingsRepository.reAuthenticate(currentUserEmail, currentPassword)
    }
}