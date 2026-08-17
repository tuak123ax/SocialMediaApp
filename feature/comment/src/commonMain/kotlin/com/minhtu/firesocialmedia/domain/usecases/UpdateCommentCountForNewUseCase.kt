package com.minhtu.firesocialmedia.domain.usecases.comment

import com.minhtu.firesocialmedia.domain.repository.CommentDbRepository

class UpdateCommentCountForNewUseCase(
    private val commentDbRepository: CommentDbRepository
) {
    suspend operator fun invoke(id: String,
                                value: Int) {
        commentDbRepository.updateCommentCountForNewInDatabase(id, value)
    }
}
