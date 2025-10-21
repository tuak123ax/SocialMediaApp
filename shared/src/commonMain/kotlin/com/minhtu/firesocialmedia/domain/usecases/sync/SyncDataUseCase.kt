package com.minhtu.firesocialmedia.domain.usecases.sync

import com.minhtu.firesocialmedia.domain.repository.CommonDbRepository
import kotlinx.coroutines.delay

class SyncDataUseCase(
    private val commonDbRepository: CommonDbRepository
) {
    suspend operator fun invoke(currentUserId : String) : Boolean {
        return commonDbRepository.syncData(currentUserId)
    }
}