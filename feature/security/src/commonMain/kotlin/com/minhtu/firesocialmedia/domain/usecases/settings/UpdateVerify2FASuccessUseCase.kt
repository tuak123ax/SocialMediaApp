package com.minhtu.firesocialmedia.domain.usecases.settings

import com.minhtu.firesocialmedia.domain.repository.SettingsRepository

class UpdateVerify2FASuccessUseCase(
    private val settingsRepository: SettingsRepository
) {
    suspend operator fun invoke() {
        settingsRepository.updateVerify2FASuccess()
    }
}
