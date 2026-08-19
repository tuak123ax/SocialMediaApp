package com.minhtu.firesocialmedia.domain.usecases.network

import com.minhtu.firesocialmedia.domain.repository.NetworkRepository
import kotlinx.coroutines.flow.Flow

class CheckInternetConnectionUseCase(
    private val networkRepository : NetworkRepository
) {
    suspend operator fun invoke() : Flow<Boolean> {
        return networkRepository.hasInternetConnection()
    }
}
