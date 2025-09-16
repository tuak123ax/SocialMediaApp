package com.minhtu.firesocialmedia.domain.usecases.comment

import com.minhtu.firesocialmedia.domain.repository.CommonDbRepository

class UpdateReplyCountForCommentUseCase(
    private val commonDbRepository: CommonDbRepository
) {
    suspend operator fun invoke(id: String,
                                currentCommentId : String,
                                value: Int) {
        commonDbRepository.updateReplyCountForCommentInDatabase(id, currentCommentId, value)
    }
}