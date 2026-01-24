package com.minhtu.firesocialmedia.domain.usecases.group

import com.minhtu.firesocialmedia.domain.repository.GroupRepository

class GetAllMembersInGroupUseCase(
    private val groupRepository: GroupRepository
) {
    suspend operator fun invoke(groupId : String) : HashMap<String, String> {
        return groupRepository.getAllMembersInGroup(groupId)
    }
}