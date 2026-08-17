package com.minhtu.firesocialmedia.domain.usecases.settings

import com.minhtu.firesocialmedia.domain.entity.authentication.TwoFAResponse
import com.minhtu.firesocialmedia.security.entity.user.UserInstance
import com.minhtu.firesocialmedia.domain.repository.SettingsRepository
import com.minhtu.firesocialmedia.domain.repository.TwoFactorAuthRepository

class Verify2FAUseCase(
    private val twoFactorAuthRepository: TwoFactorAuthRepository,
    private val settingsRepository: SettingsRepository
) {
    suspend operator fun invoke(currentUser : UserInstance,
                                otpToVerify: String) : TwoFAResponse {
        val verifyOTPResult =  twoFactorAuthRepository.verifyOTP(
            currentUser.uid,
            otpToVerify
        )
        if(verifyOTPResult.success) {
            //Update 2FA status in local storage
            settingsRepository.updateVerify2FASuccess()
        }
        return verifyOTPResult
    }
}
