package com.minhtu.firesocialmedia.core.domain.usecases.settings

import com.minhtu.firesocialmedia.core.domain.repository.SettingsRepository

class UpdateUserAvatarUseCase(private val settingsRepository: SettingsRepository) {
    suspend operator fun invoke(userId: String, imageUri: String): Boolean {
        return settingsRepository.updateUserAvatar(userId, imageUri)
    }
}

