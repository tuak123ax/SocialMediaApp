package com.minhtu.firesocialmedia.domain.usecases.comment

import com.minhtu.firesocialmedia.domain.repository.HomeCommentDbRepository

class HomeSaveLikedCommentsUseCase(
    private val commentDbRepository: HomeCommentDbRepository
) {
    suspend operator fun invoke(id : String,
                                value : HashMap<String, Int>) : Boolean {
        return commentDbRepository.saveLikedComments(id, value)
    }
}
