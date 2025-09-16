package com.minhtu.firesocialmedia.data.repository

import com.minhtu.firesocialmedia.domain.repository.LocalRepository
import com.minhtu.firesocialmedia.data.remote.service.crypto.CryptoService

class LocalRepositoryImpl(
    private val cryptoService: CryptoService
) : LocalRepository {
    override suspend fun getFCMToken(): String {
        return cryptoService.getFCMToken()
    }
}