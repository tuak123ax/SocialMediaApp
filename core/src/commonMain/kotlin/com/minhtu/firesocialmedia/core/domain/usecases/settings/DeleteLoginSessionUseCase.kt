package com.minhtu.firesocialmedia.core.domain.usecases.settings

import com.minhtu.firesocialmedia.core.domain.repository.SettingsRepository

class DeleteLoginSessionUseCase(
    private val settingsRepository: SettingsRepository
) {
    suspend operator fun invoke(userId: String, sessionId: String): Boolean {
        return settingsRepository.deleteLoginSession(userId, sessionId)
    }
}

