package com.minhtu.firesocialmedia.domain.usecases.group

import com.minhtu.firesocialmedia.domain.entity.news.NewsInstance
import com.minhtu.firesocialmedia.domain.repository.GroupRepository

class SaveNewToGroupUseCase(
    private val groupRepository: GroupRepository
) {
    suspend operator fun invoke(
        instance : NewsInstance,
        groupId : String) : Boolean {
        return groupRepository.saveNewToGroup(
            instance,
            groupId
        )
    }
}