package com.minhtu.firesocialmedia.core.domain.usecases.settings

import com.minhtu.firesocialmedia.core.domain.entity.settings.SessionItem
import com.minhtu.firesocialmedia.core.domain.repository.SettingsRepository

class FetchLoginHistoryListUseCase(
    private val settingsRepository: SettingsRepository
) {
    suspend operator fun invoke(userId : String) : List<SessionItem> {
        return settingsRepository.fetchLoginHistoryList(userId)
    }
}