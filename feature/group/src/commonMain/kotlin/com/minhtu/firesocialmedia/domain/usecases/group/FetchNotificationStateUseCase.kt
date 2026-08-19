package com.minhtu.firesocialmedia.domain.usecases.group

import com.minhtu.firesocialmedia.domain.repository.GroupRepository

class FetchNotificationStateUseCase(
    private val groupRepository: GroupRepository
) {
    suspend operator fun invoke(userId : String,
                                groupId : String) : Boolean {
        return groupRepository.fetchNotificationState(
            userId,
            groupId
        )
    }
}
