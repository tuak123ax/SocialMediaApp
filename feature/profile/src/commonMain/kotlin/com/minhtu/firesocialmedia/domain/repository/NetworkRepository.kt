package com.minhtu.firesocialmedia.domain.repository

import kotlinx.coroutines.flow.Flow

interface NetworkRepository {
    suspend fun hasInternetConnection() : Flow<Boolean>
}
