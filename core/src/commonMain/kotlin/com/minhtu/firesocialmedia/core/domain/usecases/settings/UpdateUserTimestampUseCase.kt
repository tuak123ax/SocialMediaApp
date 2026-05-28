package com.minhtu.firesocialmedia.core.domain.usecases.settings

import com.minhtu.firesocialmedia.core.domain.repository.SettingsRepository

class UpdateUserTimestampUseCase(
    private val settingsRepository: SettingsRepository
) {
    suspend fun invoke(userId: String, fieldPath: String, value: Long): Boolean {
        return settingsRepository.updateUserTimestamp(userId, fieldPath, value)
    }
}
