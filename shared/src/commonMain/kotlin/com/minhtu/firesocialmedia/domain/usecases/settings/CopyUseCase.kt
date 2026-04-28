package com.minhtu.firesocialmedia.domain.usecases.settings

import com.minhtu.firesocialmedia.domain.repository.SettingsRepository

class CopyUseCase(
    private val settingsRepository: SettingsRepository
) {
    suspend operator fun invoke(data : String) {
        settingsRepository.copy(data)
    }
}