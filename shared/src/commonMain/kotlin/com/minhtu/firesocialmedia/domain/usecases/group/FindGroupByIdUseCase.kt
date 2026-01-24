package com.minhtu.firesocialmedia.domain.usecases.group

import com.minhtu.firesocialmedia.domain.entity.group.GroupInstance
import com.minhtu.firesocialmedia.domain.repository.GroupRepository

class FindGroupByIdUseCase(
    private val groupRepository: GroupRepository
) {
    suspend operator fun invoke(groupId : String) : GroupInstance{
        return groupRepository.fetchGroupInfo(groupId)
    }
}