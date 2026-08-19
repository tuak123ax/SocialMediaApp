package com.minhtu.firesocialmedia.domain.usecases.comment

import com.minhtu.firesocialmedia.home.entity.comment.base.BaseNewsInstance
import com.minhtu.firesocialmedia.domain.repository.HomeCommentDbRepository

class HomeDeleteSubCommentFromDatabaseUseCase(
    private val commentDbRepository: HomeCommentDbRepository
) {
    suspend operator fun invoke(selectedNewId: String,
                                parentCommentId : String,
                                comment: BaseNewsInstance) {
        commentDbRepository.deleteSubCommentFromDatabase(selectedNewId, parentCommentId, comment)
    }
}
