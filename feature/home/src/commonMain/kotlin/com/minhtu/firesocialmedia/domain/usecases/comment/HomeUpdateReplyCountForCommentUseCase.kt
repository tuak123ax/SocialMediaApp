package com.minhtu.firesocialmedia.domain.usecases.comment

import com.minhtu.firesocialmedia.domain.repository.HomeCommentDbRepository

class HomeUpdateReplyCountForCommentUseCase(
    private val commentDbRepository: HomeCommentDbRepository
) {
    suspend operator fun invoke(id: String,
                                currentCommentId : String,
                                value: Int) {
        commentDbRepository.updateReplyCountForCommentInDatabase(id, currentCommentId, value)
    }
}
