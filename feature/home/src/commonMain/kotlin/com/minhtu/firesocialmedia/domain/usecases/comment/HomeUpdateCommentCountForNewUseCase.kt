package com.minhtu.firesocialmedia.domain.usecases.comment

import com.minhtu.firesocialmedia.domain.repository.HomeCommentDbRepository

class HomeUpdateCommentCountForNewUseCase(
    private val commentDbRepository: HomeCommentDbRepository
) {
    suspend operator fun invoke(id: String,
                                value: Int) {
        commentDbRepository.updateCommentCountForNewInDatabase(id, value)
    }
}
