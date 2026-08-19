package com.minhtu.firesocialmedia.domain.usecases.comment

import com.minhtu.firesocialmedia.domain.repository.CommentDbRepository

class UpdateLikeCountForSubCommentUseCase(
    private val commentDbRepository: CommentDbRepository
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
