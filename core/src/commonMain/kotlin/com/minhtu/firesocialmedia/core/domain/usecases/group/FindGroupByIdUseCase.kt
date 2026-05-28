package com.minhtu.firesocialmedia.core.domain.usecases.group

import com.minhtu.firesocialmedia.core.domain.entity.group.GroupInstance
import com.minhtu.firesocialmedia.core.domain.repository.GroupRepository

class FindGroupByIdUseCase(
    private val groupRepository: GroupRepository
) {
    suspend operator fun invoke(groupId : String) : GroupInstance{
        return groupRepository.fetchGroupInfo(groupId)
    }
}