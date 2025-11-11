package com.minhtu.firesocialmedia.domain.usecases.newsfeed

import com.minhtu.firesocialmedia.domain.repository.CommonDbRepository

class DeleteDraftPostUseCase(
    private val commonDbRepository: CommonDbRepository
) {
    suspend operator fun invoke(newId : String) : Boolean {
        return commonDbRepository.deleteDraftPost(newId)
    }
}