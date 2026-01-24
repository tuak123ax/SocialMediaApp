package com.minhtu.firesocialmedia.domain.usecases.group

import com.minhtu.firesocialmedia.domain.entity.user.UserInstance
import com.minhtu.firesocialmedia.domain.repository.GroupRepository

class InviteFriendToGroupUseCase(
    private val groupRepository: GroupRepository
) {
    suspend operator fun invoke(
        friend : UserInstance) {
        groupRepository.inviteFriendToGroup(
            friend)
    }
}