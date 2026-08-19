package com.minhtu.firesocialmedia.domain.usecases.comment

import com.minhtu.firesocialmedia.domain.repository.HomeCommentDbRepository

class HomeUpdateLikeCountForSubCommentUseCase(
    private val commentDbRepository: HomeCommentDbRepository
) {
    suspend operator fun invoke(selectedNewId : String,
                                likedComment : String,
                                parentCommentId : String,
                                value : Int) {
        commentDbRepository.updateLikeCountForSubCommentInDatabase(
            selectedNewId,
            likedComment,
            parentCommentId,
            value
        )
    }
}
