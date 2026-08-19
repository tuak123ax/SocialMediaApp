package com.minhtu.firesocialmedia.domain.usecases.sync

import com.minhtu.firesocialmedia.domain.repository.HomeCommentDbRepository
import com.minhtu.firesocialmedia.domain.repository.HomeDbRepository

class SyncDataUseCase(
    private val homeDbRepository: HomeDbRepository,
    private val commentDbRepository: HomeCommentDbRepository
) {
    suspend operator fun invoke(currentUserId : String) : Boolean {
        val likedPostsOk = homeDbRepository.syncLikedPosts(currentUserId)
        val commentsOk = commentDbRepository.syncComments()
        return likedPostsOk && commentsOk
    }
}
