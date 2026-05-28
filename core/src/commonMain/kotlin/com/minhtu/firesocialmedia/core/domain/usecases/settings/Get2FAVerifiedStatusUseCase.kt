package com.minhtu.firesocialmedia.core.domain.usecases.settings

import com.minhtu.firesocialmedia.core.domain.repository.SettingsRepository

class Get2FAVerifiedStatusUseCase(
    private val settingsRepository: SettingsRepository
) {
    suspend operator fun invoke() : Boolean {
        return settingsRepository.get2FAVerifiedStatus()
    }
}