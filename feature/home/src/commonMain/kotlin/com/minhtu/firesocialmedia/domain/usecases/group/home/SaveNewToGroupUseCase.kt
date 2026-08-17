package com.minhtu.firesocialmedia.domain.usecases.group.home

import com.minhtu.firesocialmedia.domain.repository.HomeDbRepository
import com.minhtu.firesocialmedia.home.entity.news.NewsInstance

class SaveNewToGroupUseCase(
    private val homeDbRepository: HomeDbRepository
) {
    suspend operator fun invoke(instance : NewsInstance, groupId : String) : Boolean {
        return homeDbRepository.saveNewToGroup(groupId, instance)
    }
}
