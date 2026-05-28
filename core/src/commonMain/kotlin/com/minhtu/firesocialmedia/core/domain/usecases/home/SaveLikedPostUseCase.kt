package com.minhtu.firesocialmedia.core.domain.usecases.home

import com.minhtu.firesocialmedia.core.domain.repository.CommonDbRepository

class SaveLikedPostUseCase(
    private val commonDbRepository: CommonDbRepository
) {
    suspend operator fun invoke(id : String,
                                value : HashMap<String, Int>) : Boolean {
        return commonDbRepository.saveLikedPost(id, value)
    }
}