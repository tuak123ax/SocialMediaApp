package com.minhtu.firesocialmedia.domain.usecases.group

import com.minhtu.firesocialmedia.domain.entity.group.GroupInstance
import com.minhtu.firesocialmedia.group.entity.user.UserInstance
import com.minhtu.firesocialmedia.domain.repository.GroupRepository

class RemoveMemberUseCase(
    private val groupRepository: GroupRepository
) {
    suspend operator fun invoke(member : UserInstance,
                                group : GroupInstance) : Boolean {
        return groupRepository.removeMember(member, group)
    }
}
