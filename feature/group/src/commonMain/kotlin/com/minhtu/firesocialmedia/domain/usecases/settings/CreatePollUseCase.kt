package com.minhtu.firesocialmedia.domain.usecases.settings

import com.minhtu.firesocialmedia.data.remote.mapper.settings.group.toDto
import com.minhtu.firesocialmedia.domain.entity.settings.group.PollObject
import com.minhtu.firesocialmedia.domain.repository.news.GroupNewsRepository

class CreatePollUseCase(
    private val groupNewsRepository: GroupNewsRepository
) {
    suspend operator fun invoke(poll: PollObject, newsId: String, groupId: String): Boolean {
        return groupNewsRepository.createPoll(poll.toDto(), newsId, groupId)
    }
}
