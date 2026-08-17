package com.minhtu.firesocialmedia.domain.usecases.settings

import com.minhtu.firesocialmedia.domain.entity.authentication.TwoFAResponse
import com.minhtu.firesocialmedia.security.entity.user.UserInstance
import com.minhtu.firesocialmedia.domain.repository.TwoFactorAuthRepository

class Enable2FAUseCase(
    private val twoFactorAuthRepository: TwoFactorAuthRepository
) {
    suspend operator fun invoke(currentUser : UserInstance,
                                secret : String,
                                otpToVerify: String) : TwoFAResponse {
        //Verify OTP first
        val verifyOTPResult =  twoFactorAuthRepository.enableOTP(
            currentUser.uid,
            secret,
            otpToVerify
        )
        if(verifyOTPResult.success) {
            //Verify OTP success, update twoFAEnabled flag of user
            currentUser.twoFAEnabled = twoFactorAuthRepository.updateTwoFAEnabledFlagForUser(
                currentUser.uid,
                true
            )
        }
        return verifyOTPResult
    }
}
