package com.minhtu.firesocialmedia.domain.usecases.settings

import com.minhtu.firesocialmedia.domain.entity.authentication.TwoFAResponse
import com.minhtu.firesocialmedia.domain.entity.user.UserInstance
import com.minhtu.firesocialmedia.domain.repository.SettingsRepository

class Enable2FAUseCase(
    private val settingsRepository: SettingsRepository
) {
    suspend operator fun invoke(currentUser : UserInstance,
                                secret : String,
                                otpToVerify: String) : TwoFAResponse {
        //Verify OTP first
        val verifyOTPResult =  settingsRepository.enableOTP(
            currentUser.uid,
            secret,
            otpToVerify
        )
        if(verifyOTPResult.success) {
            //Verify OTP success, update twoFAEnabled flag of user
            currentUser.twoFAEnabled = settingsRepository.updateTwoFAEnabledFlagForUser(
                currentUser.uid,
                true
            )
        }
        return verifyOTPResult
    }
}