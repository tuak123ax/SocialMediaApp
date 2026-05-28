package com.minhtu.firesocialmedia.core.domain.usecases.comment

import com.minhtu.firesocialmedia.core.domain.repository.CommonDbRepository

class UpdateLikeCountForSubCommentUseCase(
    private val commonDbRepository: CommonDbRepository
) {
    suspend operator fun invoke(selectedNewId : String,
                                likedComment : String,
                                parentCommentId : String,
                                value : Int) {
        commonDbRepository.updateLikeCountForSubCommentInDatabase(
            selectedNewId,
            likedComment,
            parentCommentId,
            value
        )
    }
}