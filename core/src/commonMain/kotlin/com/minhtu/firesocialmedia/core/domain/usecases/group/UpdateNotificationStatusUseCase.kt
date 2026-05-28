package com.minhtu.firesocialmedia.core.domain.usecases.group

import com.minhtu.firesocialmedia.core.domain.repository.GroupRepository

class UpdateNotificationStatusUseCase(
    private val groupRepository: GroupRepository
) {
    suspend operator fun invoke(newStatus : Boolean,
                                groupId : String,
                                userId : String) : Boolean {
        return groupRepository.updateNotificationStatus(newStatus, groupId, userId)
    }
}