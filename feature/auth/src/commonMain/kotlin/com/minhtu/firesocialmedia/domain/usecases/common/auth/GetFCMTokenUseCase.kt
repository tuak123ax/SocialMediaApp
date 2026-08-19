package com.minhtu.firesocialmedia.domain.usecases.common.auth
import com.minhtu.firesocialmedia.data.local.service.crypto.AuthCryptoService

class GetFCMTokenUseCase(
    private val cryptoService: AuthCryptoService
) {
    suspend operator fun invoke() : String{
        return cryptoService.getFCMToken()
    }
}
