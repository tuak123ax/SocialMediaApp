package com.minhtu.firesocialmedia.core.domain.usecases.comment

import com.minhtu.firesocialmedia.core.domain.repository.CommonDbRepository

class SaveLikedCommentsUseCase(
    private val commonDbRepository: CommonDbRepository
) {
    suspend operator fun invoke(id : String,
                                value : HashMap<String, Int>) : Boolean {
        return commonDbRepository.saveLikedComments(id, value)
    }
}