package com.minhtu.firesocialmedia.core.domain.usecases.settings

import com.minhtu.firesocialmedia.core.domain.repository.SettingsRepository

class CopyUseCase(
    private val settingsRepository: SettingsRepository
) {
    suspend operator fun invoke(data : String) {
        settingsRepository.copy(data)
    }
}