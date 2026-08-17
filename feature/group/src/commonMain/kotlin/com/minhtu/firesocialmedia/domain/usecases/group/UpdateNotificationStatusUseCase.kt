package com.minhtu.firesocialmedia.domain.usecases.group

import com.minhtu.firesocialmedia.domain.repository.GroupRepository

class UpdateNotificationStatusUseCase(
    private val groupRepository: GroupRepository
) {
    suspend operator fun invoke(newStatus : Boolean,
                                groupId : String,
                                userId : String) : Boolean {
        return groupRepository.updateNotificationStatus(newStatus, groupId, userId)
    }
}
