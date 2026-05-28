package com.minhtu.firesocialmedia.core.domain.repository

import kotlinx.coroutines.flow.Flow

interface NetworkRepository {
    suspend fun hasInternetConnection() : Flow<Boolean>
}