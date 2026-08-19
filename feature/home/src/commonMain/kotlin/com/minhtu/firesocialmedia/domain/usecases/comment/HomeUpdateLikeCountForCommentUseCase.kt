package com.minhtu.firesocialmedia.domain.usecases.comment

import com.minhtu.firesocialmedia.domain.repository.HomeCommentDbRepository

class HomeUpdateLikeCountForCommentUseCase(
    private val commentDbRepository: HomeCommentDbRepository
) {
    suspend operator fun invoke(selectedNewId : String,
                                likedComment : String,
                                value : Int) {
        commentDbRepository.updateLikeCountForCommentInDatabase(
            selectedNewId,
            likedComment,
            value
        )
    }
}
