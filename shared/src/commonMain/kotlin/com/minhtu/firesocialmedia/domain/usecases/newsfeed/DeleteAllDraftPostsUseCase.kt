package com.minhtu.firesocialmedia.domain.usecases.newsfeed

import com.minhtu.firesocialmedia.domain.repository.CommonDbRepository

class DeleteAllDraftPostsUseCase(
    private val commonDbRepository: CommonDbRepository
) {
    suspend operator fun invoke() : Boolean {
        return commonDbRepository.deleteAllDraftPosts()
    }
}