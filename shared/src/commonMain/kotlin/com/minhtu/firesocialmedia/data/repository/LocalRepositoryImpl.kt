package com.minhtu.firesocialmedia.data.repository

import com.minhtu.firesocialmedia.domain.repository.LocalRepository
import com.minhtu.firesocialmedia.domain.service.crypto.CryptoService
import com.minhtu.firesocialmedia.domain.usecases.common.GetFCMTokenUseCase

class LocalRepositoryImpl(
    private val cryptoService: CryptoService
) : LocalRepository {
    override suspend fun getFCMToken(): String {
        return cryptoService.getFCMToken()
    }
}