package com.minhtu.firesocialmedia.data.remote.service.auth

import com.minhtu.firesocialmedia.domain.entity.authentication.TwoFAResponse

interface TwoFactorAuthService {
    suspend fun generateSecretFor2FA(): String
    suspend fun enableOTP(userId: String, secret: String, otpToVerify: String): TwoFAResponse
    suspend fun verifyOTP(userId: String, otpToVerify: String): TwoFAResponse
    suspend fun disable2FA(userId: String): TwoFAResponse
    suspend fun verifyBackupCode(userId: String, backupCode: String): TwoFAResponse
}
