package com.minhtu.firesocialmedia.domain.usecases.group

import com.minhtu.firesocialmedia.domain.entity.group.GroupInstance
import com.minhtu.firesocialmedia.domain.repository.GroupRepository

class CreateGroupUseCase(
    private val groupRepository: GroupRepository
) {
    suspend operator fun invoke(
        group : GroupInstance,
        userId : String) : Boolean {
        return groupRepository.saveGroupAndUserGroups(
            group,
            userId)
    }
}