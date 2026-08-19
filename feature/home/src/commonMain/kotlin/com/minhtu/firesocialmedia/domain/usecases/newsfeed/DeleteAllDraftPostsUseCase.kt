package com.minhtu.firesocialmedia.domain.usecases.newsfeed

import com.minhtu.firesocialmedia.domain.repository.HomeDbRepository

class DeleteAllDraftPostsUseCase(
    private val homeDbRepository: HomeDbRepository
) {
    suspend operator fun invoke() : Boolean {
        return homeDbRepository.deleteAllDraftPosts()
    }
}
