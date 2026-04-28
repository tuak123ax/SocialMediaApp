package com.minhtu.firesocialmedia.domain.usecases.settings

import com.minhtu.firesocialmedia.domain.entity.authentication.TwoFAResponse
import com.minhtu.firesocialmedia.domain.repository.SettingsRepository

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