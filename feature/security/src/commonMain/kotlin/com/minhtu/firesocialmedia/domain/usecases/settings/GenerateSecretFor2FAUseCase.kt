package com.minhtu.firesocialmedia.domain.usecases.settings

import com.minhtu.firesocialmedia.domain.repository.TwoFactorAuthRepository

class GenerateSecretFor2FAUseCase(
    private val twoFactorAuthRepository: TwoFactorAuthRepository
) {
    suspend operator fun invoke() : String {
        return twoFactorAuthRepository.generateSecretFor2FA()
    }
}
