package com.minhtu.firesocialmedia.core.domain.usecases.newsfeed

import com.minhtu.firesocialmedia.core.domain.repository.CommonDbRepository

class DeleteDraftPostUseCase(
    private val commonDbRepository: CommonDbRepository
) {
    suspend operator fun invoke(newId : String) : Boolean {
        return commonDbRepository.deleteDraftPost(newId)
    }
}