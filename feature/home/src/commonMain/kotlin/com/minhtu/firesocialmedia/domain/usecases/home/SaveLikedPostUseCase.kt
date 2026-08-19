package com.minhtu.firesocialmedia.domain.usecases.home

import com.minhtu.firesocialmedia.domain.repository.HomeDbRepository

class SaveLikedPostUseCase(
    private val homeDbRepository: HomeDbRepository
) {
    suspend operator fun invoke(id : String,
                                value : HashMap<String, Int>) : Boolean {
        return homeDbRepository.saveLikedPost(id, value)
    }
}
