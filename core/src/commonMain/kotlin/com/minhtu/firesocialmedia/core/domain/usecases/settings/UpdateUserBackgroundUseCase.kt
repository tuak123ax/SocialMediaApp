package com.minhtu.firesocialmedia.core.domain.usecases.settings

import com.minhtu.firesocialmedia.core.domain.repository.SettingsRepository

class UpdateUserBackgroundUseCase(private val settingsRepository: SettingsRepository) {
    suspend operator fun invoke(userId: String, imageUri: String): Boolean {
        return settingsRepository.updateUserBackground(userId, imageUri)
    }
}

