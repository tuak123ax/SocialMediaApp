package com.minhtu.firesocialmedia.domain.usecases.settings

import com.minhtu.firesocialmedia.domain.repository.SettingsRepository

class Get2FAVerifiedStatusUseCase(
    private val settingsRepository: SettingsRepository
) {
    suspend operator fun invoke() : Boolean {
        return settingsRepository.get2FAVerifiedStatus()
    }
}
