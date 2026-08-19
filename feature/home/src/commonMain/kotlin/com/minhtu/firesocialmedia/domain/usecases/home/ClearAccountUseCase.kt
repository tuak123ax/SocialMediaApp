package com.minhtu.firesocialmedia.domain.usecases.home

import com.minhtu.firesocialmedia.data.local.service.crypto.HomeCryptoService

class ClearAccountUseCase(
    private val cryptoService: HomeCryptoService
) {
    suspend operator fun invoke() {
        cryptoService.clearAccount()
    }
}
