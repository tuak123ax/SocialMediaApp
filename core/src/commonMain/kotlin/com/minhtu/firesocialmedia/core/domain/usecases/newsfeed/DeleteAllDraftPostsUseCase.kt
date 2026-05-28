package com.minhtu.firesocialmedia.core.domain.usecases.newsfeed

import com.minhtu.firesocialmedia.core.domain.repository.CommonDbRepository

class DeleteAllDraftPostsUseCase(
    private val commonDbRepository: CommonDbRepository
) {
    suspend operator fun invoke() : Boolean {
        return commonDbRepository.deleteAllDraftPosts()
    }
}