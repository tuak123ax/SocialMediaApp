package com.minhtu.firesocialmedia.domain.usecases.settings

import com.minhtu.firesocialmedia.domain.entity.settings.SessionItem
import com.minhtu.firesocialmedia.domain.repository.SecuritySettingsRepository

class FetchLoginHistoryListUseCase(
    private val settingsRepository: SecuritySettingsRepository
) {
    suspend operator fun invoke(userId : String) : List<SessionItem> {
        return settingsRepository.fetchLoginHistoryList(userId)
    }
}
