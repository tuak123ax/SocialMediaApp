package com.minhtu.firesocialmedia.core.domain.usecases.group

import com.minhtu.firesocialmedia.core.domain.entity.group.GroupInstance
import com.minhtu.firesocialmedia.core.domain.repository.GroupRepository

class GetAllGroupsUseCase(
    private val groupRepository: GroupRepository
) {
    suspend operator fun invoke(userId : String) : Set<GroupInstance>{
        return groupRepository.getAllGroups(userId)
    }
}