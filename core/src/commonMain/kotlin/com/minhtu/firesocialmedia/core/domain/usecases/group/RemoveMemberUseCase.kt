package com.minhtu.firesocialmedia.core.domain.usecases.group

import com.minhtu.firesocialmedia.core.domain.entity.group.GroupInstance
import com.minhtu.firesocialmedia.core.domain.entity.user.UserInstance
import com.minhtu.firesocialmedia.core.domain.repository.GroupRepository

class RemoveMemberUseCase(
    private val groupRepository: GroupRepository
) {
    suspend operator fun invoke(member : UserInstance,
                                group : GroupInstance) : Boolean {
        return groupRepository.removeMember(member, group)
    }
}