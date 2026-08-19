package com.minhtu.firesocialmedia.domain.usecases.group.home

import com.minhtu.firesocialmedia.domain.repository.HomeDbRepository

class GetGroupConfigsUseCase(
    private val homeDbRepository: HomeDbRepository
) {
    suspend operator fun invoke(userId : String, groupId: String) : Boolean {
        return homeDbRepository.isGroupNotificationOnForUser(userId, groupId)
    }
}
