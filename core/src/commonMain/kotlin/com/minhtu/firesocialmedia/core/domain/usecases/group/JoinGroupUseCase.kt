package com.minhtu.firesocialmedia.core.domain.usecases.group

import com.minhtu.firesocialmedia.core.domain.entity.group.GroupInstance
import com.minhtu.firesocialmedia.core.domain.entity.user.UserInstance
import com.minhtu.firesocialmedia.core.domain.repository.GroupRepository

class JoinGroupUseCase(
    private val groupRepository: GroupRepository
) {
    suspend operator fun invoke(user : UserInstance, group : GroupInstance) : Boolean {
        return groupRepository.joinGroup(user, group)
    }
}