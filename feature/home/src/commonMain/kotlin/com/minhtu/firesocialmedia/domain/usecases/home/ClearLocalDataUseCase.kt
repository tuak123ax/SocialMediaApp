package com.minhtu.firesocialmedia.domain.usecases.home

import com.minhtu.firesocialmedia.domain.repository.HomeCommentDbRepository
import com.minhtu.firesocialmedia.domain.repository.HomeDbRepository

class ClearLocalDataUseCase(
    private val homeDbRepository: HomeDbRepository,
    private val commentDbRepository: HomeCommentDbRepository
) {
    suspend operator fun invoke() {
        homeDbRepository.clearLikedPosts()
        commentDbRepository.clearComments()
    }
}
