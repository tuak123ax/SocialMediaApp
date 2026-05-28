package com.minhtu.firesocialmedia.core.domain.usecases.group

import com.minhtu.firesocialmedia.core.domain.entity.user.UserInstance
import com.minhtu.firesocialmedia.core.domain.repository.GroupRepository

class InviteFriendToGroupUseCase(
    private val groupRepository: GroupRepository
) {
    suspend operator fun invoke(
        friend : UserInstance) {
        groupRepository.inviteFriendToGroup(
            friend)
    }
}