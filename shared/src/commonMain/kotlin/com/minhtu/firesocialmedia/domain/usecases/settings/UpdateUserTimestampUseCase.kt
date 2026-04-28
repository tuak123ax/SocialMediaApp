package com.minhtu.firesocialmedia.domain.usecases.settings

import com.minhtu.firesocialmedia.domain.repository.SettingsRepository

class UpdateUserTimestampUseCase(
    private val settingsRepository: SettingsRepository
) {
    suspend fun invoke(userId: String, fieldPath: String, value: Long): Boolean {
        return settingsRepository.updateUserTimestamp(userId, fieldPath, value)
    }
}
