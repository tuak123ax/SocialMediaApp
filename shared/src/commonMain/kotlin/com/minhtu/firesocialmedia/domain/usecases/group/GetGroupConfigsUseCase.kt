package com.minhtu.firesocialmedia.domain.usecases.group

import com.minhtu.firesocialmedia.domain.entity.group.GroupConfigs
import com.minhtu.firesocialmedia.domain.repository.GroupRepository

class GetGroupConfigsUseCase(
    private val groupRepository: GroupRepository
) {
    suspend operator fun invoke(userId : String,
                                groupId: String) : GroupConfigs {
        return groupRepository.getGroupConfigs(userId, groupId)
    }
}