package com.minhtu.firesocialmedia.domain.usecases.comment

import com.minhtu.firesocialmedia.domain.entity.base.BaseNewsInstance
import com.minhtu.firesocialmedia.domain.repository.CommentDbRepository

class DeleteSubCommentFromDatabaseUseCase(
    private val commentDbRepository: CommentDbRepository
) {
    suspend operator fun invoke(selectedNewId: String,
                                parentCommentId : String,
                                comment: BaseNewsInstance) {
        commentDbRepository.deleteSubCommentFromDatabase(selectedNewId, parentCommentId, comment)
    }
}
