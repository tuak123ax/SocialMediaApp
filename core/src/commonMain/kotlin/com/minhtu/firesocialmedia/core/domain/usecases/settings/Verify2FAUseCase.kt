package com.minhtu.firesocialmedia.core.domain.usecases.settings

import com.minhtu.firesocialmedia.core.domain.entity.authentication.TwoFAResponse
import com.minhtu.firesocialmedia.core.domain.entity.user.UserInstance
import com.minhtu.firesocialmedia.core.domain.repository.SettingsRepository

class Verify2FAUseCase(
    private val settingsRepository: SettingsRepository
) {
    suspend operator fun invoke(currentUser : UserInstance,
                                otpToVerify: String) : TwoFAResponse {
        val verifyOTPResult =  settingsRepository.verifyOTP(
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