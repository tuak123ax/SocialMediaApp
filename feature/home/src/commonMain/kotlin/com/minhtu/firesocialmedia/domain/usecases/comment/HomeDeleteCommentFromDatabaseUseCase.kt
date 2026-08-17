package com.minhtu.firesocialmedia.domain.usecases.comment

import com.minhtu.firesocialmedia.home.entity.comment.base.BaseNewsInstance
import com.minhtu.firesocialmedia.domain.repository.HomeCommentDbRepository

class HomeDeleteCommentFromDatabaseUseCase(
    private val commentDbRepository: HomeCommentDbRepository
) {
    suspend operator fun invoke(selectedNewId: String,
                                comment: BaseNewsInstance) {
        commentDbRepository.deleteCommentFromDatabase(selectedNewId, comment)
    }
}
