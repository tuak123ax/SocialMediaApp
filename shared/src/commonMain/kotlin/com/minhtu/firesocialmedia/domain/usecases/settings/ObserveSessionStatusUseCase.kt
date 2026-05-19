package com.minhtu.firesocialmedia.domain.usecases.settings

import com.minhtu.firesocialmedia.domain.repository.SettingsRepository

class ObserveSessionStatusUseCase(
    private val settingsRepository: SettingsRepository
) {
    operator fun invoke(userId: String, sessionId: String, onLoggedOut: () -> Unit) {
        settingsRepository.observeSessionStatus(userId, sessionId, onLoggedOut)
    }
}

class StopObserveSessionStatusUseCase(
    private val settingsRepository: SettingsRepository
) {
    operator fun invoke() {
        settingsRepository.stopObserveSessionStatus()
    }
}

