package com.minhtu.firesocialmedia.core.domain.usecases.network

import com.minhtu.firesocialmedia.core.domain.repository.NetworkRepository
import kotlinx.coroutines.flow.Flow

class CheckInternetConnectionUseCase(
    private val networkRepository : NetworkRepository
) {
    suspend operator fun invoke() : Flow<Boolean> {
        return networkRepository.hasInternetConnection()
    }
}