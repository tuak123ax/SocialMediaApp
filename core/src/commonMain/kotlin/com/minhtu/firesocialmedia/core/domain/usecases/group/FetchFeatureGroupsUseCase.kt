package com.minhtu.firesocialmedia.core.domain.usecases.group

import com.minhtu.firesocialmedia.core.domain.entity.group.GroupInstance
import com.minhtu.firesocialmedia.core.domain.repository.GroupRepository

class FetchFeatureGroupsUseCase(
    private val groupRepository: GroupRepository
) {
    suspend operator fun invoke(limit: Int) : List<GroupInstance> {
        return groupRepository.fetchFeatureGroups(limit)
    }
}