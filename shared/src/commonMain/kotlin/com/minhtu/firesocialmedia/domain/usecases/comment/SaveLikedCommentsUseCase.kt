package com.minhtu.firesocialmedia.domain.usecases.comment

import com.minhtu.firesocialmedia.domain.repository.CommonDbRepository

class SaveLikedCommentsUseCase(
    private val commonDbRepository: CommonDbRepository
) {
    suspend operator fun invoke(id : String,
                                value : HashMap<String, Int>) : Boolean {
        return commonDbRepository.saveLikedComments(id, value)
    }
}