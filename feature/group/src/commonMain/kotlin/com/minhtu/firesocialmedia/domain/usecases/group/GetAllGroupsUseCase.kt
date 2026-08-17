package com.minhtu.firesocialmedia.domain.usecases.group

import com.minhtu.firesocialmedia.domain.entity.group.GroupInstance
import com.minhtu.firesocialmedia.domain.repository.GroupRepository

class GetAllGroupsUseCase(
    private val groupRepository: GroupRepository
) {
    suspend operator fun invoke(userId : String) : Set<GroupInstance>{
        return groupRepository.getAllGroups(userId)
    }
}
