package com.minhtu.firesocialmedia.domain.usecases.settings

import com.minhtu.firesocialmedia.domain.repository.SettingsRepository

class GenerateSecretFor2FAUseCase(
    private val settingsRepository: SettingsRepository
) {
    suspend operator fun invoke() : String {
        return settingsRepository.generateSecretFor2FA()
    }
}