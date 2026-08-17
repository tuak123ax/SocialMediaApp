package com.minhtu.firesocialmedia.domain.usecases.comment

import com.minhtu.firesocialmedia.domain.repository.CommentDbRepository

class UpdateLikeCountForCommentUseCase(
    private val commentDbRepository: CommentDbRepository
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
