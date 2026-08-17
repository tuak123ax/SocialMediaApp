package com.minhtu.firesocialmedia.data.repository

import com.minhtu.firesocialmedia.constants.security.DataConstant
import com.minhtu.firesocialmedia.data.remote.service.auth.TwoFactorAuthService
import com.minhtu.firesocialmedia.data.remote.service.database.SecurityDatabaseService
import com.minhtu.firesocialmedia.domain.entity.authentication.TwoFAResponse
import com.minhtu.firesocialmedia.domain.repository.TwoFactorAuthRepository

class TwoFactorAuthRepositoryImpl(
    private val twoFactorAuthService: TwoFactorAuthService,
    private val databaseService: SecurityDatabaseService
) : TwoFactorAuthRepository {
    override suspend fun generateSecretFor2FA(): String {
        return twoFactorAuthService.generateSecretFor2FA()
    }

    override suspend fun enableOTP(userId: String, secret: String, otpToVerify: String): TwoFAResponse {
        return twoFactorAuthService.enableOTP(userId, secret, otpToVerify)
    }

    override suspend fun verifyOTP(userId: String, otpToVerify: String): TwoFAResponse {
        return twoFactorAuthService.verifyOTP(userId, otpToVerify)
    }

    override suspend fun updateTwoFAEnabledFlagForUser(userId: String, twoFAEnabled: Boolean): Boolean {
        return databaseService.updateTwoFAEnabledFlagForUser(
            userId,
            twoFAEnabled,
            DataConstant.USER_PATH,
            DataConstant.TWO_FA_ENABLED_PATH
        )
    }

    override suspend fun disable2FA(userId: String): TwoFAResponse {
        return twoFactorAuthService.disable2FA(userId)
    }

    override suspend fun verifyBackupCode(userId: String, backupCode: String): TwoFAResponse {
        return twoFactorAuthService.verifyBackupCode(userId, backupCode)
    }
}
