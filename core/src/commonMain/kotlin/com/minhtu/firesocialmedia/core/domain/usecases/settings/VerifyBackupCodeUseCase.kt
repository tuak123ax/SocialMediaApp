package com.minhtu.firesocialmedia.core.domain.usecases.settings

import com.minhtu.firesocialmedia.core.domain.entity.authentication.TwoFAResponse
import com.minhtu.firesocialmedia.core.domain.repository.SettingsRepository

class VerifyBackupCodeUseCase(
    private val settingsRepository: SettingsRepository
) {
    suspend operator fun invoke(
        userId: String,
        backupCode: String
    ): TwoFAResponse {
        val verifyBackupCode = settingsRepository.verifyBackupCode(userId, backupCode)
        if(verifyBackupCode.success) {
            //Update 2FA status in local storage
            settingsRepository.updateVerify2FASuccess()
        }
        return verifyBackupCode
    }
}