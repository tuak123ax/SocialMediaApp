package com.minhtu.firesocialmedia.domain.usecases.group

import com.minhtu.firesocialmedia.domain.entity.group.GroupInstance
import com.minhtu.firesocialmedia.domain.entity.user.UserInstance
import com.minhtu.firesocialmedia.domain.repository.GroupRepository

class LeaveAndDeleteGroupUseCase(
    private val groupRepository: GroupRepository
) {
    suspend operator fun invoke(user : UserInstance,
                                group : GroupInstance) : Boolean {
        return groupRepository.leaveAndDeleteGroup(
            user,
            group
        )
    }
}