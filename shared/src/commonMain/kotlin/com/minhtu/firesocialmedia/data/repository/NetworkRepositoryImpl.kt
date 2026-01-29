package com.minhtu.firesocialmedia.data.repository

import com.minhtu.firesocialmedia.domain.core.NetworkMonitor
import com.minhtu.firesocialmedia.domain.repository.NetworkRepository
import kotlinx.coroutines.flow.Flow

class NetworkRepositoryImpl(
    private val networkMonitor: NetworkMonitor
) : NetworkRepository {
    override suspend fun hasInternetConnection(): Flow<Boolean> {
        return networkMonitor.isOnline
    }
}