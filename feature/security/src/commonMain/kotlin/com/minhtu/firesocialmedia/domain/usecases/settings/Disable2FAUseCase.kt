package com.minhtu.firesocialmedia.domain.usecases.settings

import com.minhtu.firesocialmedia.domain.entity.authentication.TwoFAResponse
import com.minhtu.firesocialmedia.security.entity.user.UserInstance
import com.minhtu.firesocialmedia.domain.repository.SettingsRepository
import com.minhtu.firesocialmedia.domain.repository.TwoFactorAuthRepository

class Disable2FAUseCase(
    private val twoFactorAuthRepository: TwoFactorAuthRepository,
    private val settingsRepository: SettingsRepository
) {
    suspend operator fun invoke(
        currentUser : UserInstance
    ) : TwoFAResponse{
        //Verify OTP first
        val disable2FAResult =  twoFactorAuthRepository.disable2FA(
            currentUser.uid
        )
        if(disable2FAResult.success) {
            //Disable 2FA success, update twoFAEnabled flag of user
            currentUser.twoFAEnabled = twoFactorAuthRepository.updateTwoFAEnabledFlagForUser(
                currentUser.uid,
                false
            )
            //Delete local 2FA status in local storage
            settingsRepository.delete2FAStatusInLocal()
        }
        return disable2FAResult
    }
}
