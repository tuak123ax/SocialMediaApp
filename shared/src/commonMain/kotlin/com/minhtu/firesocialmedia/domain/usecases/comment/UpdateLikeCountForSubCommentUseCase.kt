package com.minhtu.firesocialmedia.domain.usecases.comment

import com.minhtu.firesocialmedia.domain.repository.CommonDbRepository

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