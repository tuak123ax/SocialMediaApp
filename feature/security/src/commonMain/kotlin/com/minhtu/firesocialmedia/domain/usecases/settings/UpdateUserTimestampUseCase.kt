package com.minhtu.firesocialmedia.domain.usecases.settings

import com.minhtu.firesocialmedia.domain.repository.SecuritySettingsRepository

class UpdateUserTimestampUseCase(
    private val settingsRepository: SecuritySettingsRepository
) {
    suspend fun invoke(userId: String, fieldPath: String, value: Long): Boolean {
        return settingsRepository.updateUserTimestamp(userId, fieldPath, value)
    }
}
