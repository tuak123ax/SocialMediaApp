package com.minhtu.firesocialmedia.domain.usecases.comment

import com.minhtu.firesocialmedia.domain.entity.base.BaseNewsInstance
import com.minhtu.firesocialmedia.domain.repository.CommentDbRepository

class DeleteCommentFromDatabaseUseCase(
    private val commentDbRepository: CommentDbRepository
) {
    suspend operator fun invoke(selectedNewId: String,
                                comment: BaseNewsInstance) {
        commentDbRepository.deleteCommentFromDatabase(selectedNewId, comment)
    }
}
