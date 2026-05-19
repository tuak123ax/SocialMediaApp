package com.minhtu.firesocialmedia.domain.usecases.settings

import com.minhtu.firesocialmedia.domain.repository.SettingsRepository

class DeleteLoginSessionUseCase(
    private val settingsRepository: SettingsRepository
) {
    suspend operator fun invoke(userId: String, sessionId: String): Boolean {
        return settingsRepository.deleteLoginSession(userId, sessionId)
    }
}

