package com.minhtu.firesocialmedia.domain.usecases.comment

import com.minhtu.firesocialmedia.domain.repository.CommonDbRepository

class UpdateLikeCountForCommentUseCase(
    private val commonDbRepository: CommonDbRepository
) {
    suspend operator fun invoke(selectedNewId : String,
                                likedComment : String,
                                value : Int) {
        commonDbRepository.updateLikeCountForCommentInDatabase(
            selectedNewId,
            likedComment,
            value
        )
    }
}