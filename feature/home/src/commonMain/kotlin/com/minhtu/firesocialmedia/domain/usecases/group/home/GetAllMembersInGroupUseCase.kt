package com.minhtu.firesocialmedia.domain.usecases.group.home

import com.minhtu.firesocialmedia.domain.repository.HomeDbRepository

class GetAllMembersInGroupUseCase(
    private val homeDbRepository: HomeDbRepository
) {
    suspend operator fun invoke(groupId : String) : HashMap<String, String> {
        return homeDbRepository.getAllMembersInGroup(groupId)
    }
}
