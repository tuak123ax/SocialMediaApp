package com.minhtu.firesocialmedia.core.domain.usecases.group

import com.minhtu.firesocialmedia.core.domain.entity.group.GroupConfigs
import com.minhtu.firesocialmedia.core.domain.repository.GroupRepository

class GetGroupConfigsUseCase(
    private val groupRepository: GroupRepository
) {
    suspend operator fun invoke(userId : String,
                                groupId: String) : GroupConfigs {
        return groupRepository.getGroupConfigs(userId, groupId)
    }
}