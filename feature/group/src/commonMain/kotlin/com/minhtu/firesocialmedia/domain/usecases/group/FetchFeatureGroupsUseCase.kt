package com.minhtu.firesocialmedia.domain.usecases.group

import com.minhtu.firesocialmedia.domain.entity.group.GroupInstance
import com.minhtu.firesocialmedia.domain.repository.GroupRepository

class FetchFeatureGroupsUseCase(
    private val groupRepository: GroupRepository
) {
    suspend operator fun invoke(limit: Int) : List<GroupInstance> {
        return groupRepository.fetchFeatureGroups(limit)
    }
}
