package com.minhtu.firesocialmedia.core.domain.usecases.group

import com.minhtu.firesocialmedia.core.domain.entity.news.NewsInstance
import com.minhtu.firesocialmedia.core.domain.repository.GroupRepository

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