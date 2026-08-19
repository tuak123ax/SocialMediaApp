package com.minhtu.firesocialmedia.domain.usecases.home.security

import com.minhtu.firesocialmedia.data.local.service.crypto.SecurityCryptoService

class ClearAccountUseCase(
    private val cryptoService: SecurityCryptoService
) {
    suspend operator fun invoke() {
        cryptoService.clearAccount()
    }
}
