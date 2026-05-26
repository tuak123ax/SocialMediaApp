package com.minhtu.firesocialmedia.domain.usecases.settings

import com.minhtu.firesocialmedia.domain.repository.SettingsRepository

class UpdateUserAvatarUseCase(private val settingsRepository: SettingsRepository) {
    suspend operator fun invoke(userId: String, imageUri: String): Boolean {
        return settingsRepository.updateUserAvatar(userId, imageUri)
    }
}

