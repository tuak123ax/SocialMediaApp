package com.minhtu.firesocialmedia.core.domain.usecases.comment

import com.minhtu.firesocialmedia.core.domain.repository.CommonDbRepository

class UpdateReplyCountForCommentUseCase(
    private val commonDbRepository: CommonDbRepository
) {
    suspend operator fun invoke(id: String,
                                currentCommentId : String,
                                value: Int) {
        commonDbRepository.updateReplyCountForCommentInDatabase(id, currentCommentId, value)
    }
}