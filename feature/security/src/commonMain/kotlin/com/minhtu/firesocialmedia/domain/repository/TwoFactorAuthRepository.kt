package com.minhtu.firesocialmedia.domain.repository

import com.minhtu.firesocialmedia.domain.entity.authentication.TwoFAResponse

interface TwoFactorAuthRepository {
    suspend fun generateSecretFor2FA(): String
    suspend fun enableOTP(userId: String, secret: String, otpToVerify: String): TwoFAResponse
    suspend fun verifyOTP(userId: String, otpToVerify: String): TwoFAResponse
    suspend fun updateTwoFAEnabledFlagForUser(userId: String, twoFAEnabled: Boolean): Boolean
    suspend fun disable2FA(userId: String): TwoFAResponse
    suspend fun verifyBackupCode(userId: String, backupCode: String): TwoFAResponse
}
