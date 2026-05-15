package com.minhtu.firesocialmedia.domain.usecases.settings

import com.minhtu.firesocialmedia.domain.repository.SettingsRepository

class UpdateUserBackgroundUseCase(private val settingsRepository: SettingsRepository) {
    suspend operator fun invoke(userId: String, imageUri: String): Boolean {
        return settingsRepository.updateUserBackground(userId, imageUri)
    }
}

