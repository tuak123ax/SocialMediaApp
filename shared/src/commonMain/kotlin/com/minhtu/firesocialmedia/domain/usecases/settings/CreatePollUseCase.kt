package com.minhtu.firesocialmedia.domain.usecases.settings

import com.minhtu.firesocialmedia.domain.entity.settings.PollObject
import com.minhtu.firesocialmedia.domain.repository.SettingsRepository

class CreatePollUseCase(
    private val settingsRepository: SettingsRepository
) {
    suspend operator fun invoke(poll: PollObject, newsId: String, groupId: String): Boolean {
        return settingsRepository.createPoll(poll, newsId, groupId)
    }
}