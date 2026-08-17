package com.minhtu.firesocialmedia.domain.usecases.settings

import com.minhtu.firesocialmedia.domain.repository.SecuritySettingsRepository

class LogoutSessionUseCase(
    private val settingsRepository: SecuritySettingsRepository
) {
    suspend operator fun invoke(userId: String, sessionId: String): Boolean {
        return settingsRepository.logoutSession(userId, sessionId)
    }
}
