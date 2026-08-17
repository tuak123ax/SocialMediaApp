package com.minhtu.firesocialmedia.domain.usecases.newsfeed

import com.minhtu.firesocialmedia.domain.repository.HomeDbRepository

class DeleteDraftPostUseCase(
    private val homeDbRepository: HomeDbRepository
) {
    suspend operator fun invoke(newId : String) : Boolean {
        return homeDbRepository.deleteDraftPost(newId)
    }
}
