package com.minhtu.firesocialmedia.domain.usecases.settings

import com.minhtu.firesocialmedia.domain.repository.SettingsRepository

class UpdateUserStringFieldUseCase(
    private val settingsRepository: SettingsRepository
) {
    suspend operator fun invoke(userId: String, fieldPath: String, value: String): Boolean {
        return settingsRepository.updateUserStringField(userId, fieldPath, value)
    }
}

