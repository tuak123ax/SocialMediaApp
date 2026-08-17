package com.minhtu.firesocialmedia.domain.usecases.settings

import com.minhtu.firesocialmedia.domain.repository.SecuritySettingsRepository

class CopyUseCase(
    private val settingsRepository: SecuritySettingsRepository
) {
    suspend operator fun invoke(data : String) {
        settingsRepository.copy(data)
    }
}
