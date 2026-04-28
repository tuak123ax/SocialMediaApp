package com.minhtu.firesocialmedia.domain.repository

import com.minhtu.firesocialmedia.domain.entity.authentication.TwoFAResponse
import com.minhtu.firesocialmedia.domain.entity.settings.ChangePasswordState
import com.minhtu.firesocialmedia.domain.entity.settings.SessionItem
import com.minhtu.firesocialmedia.domain.entity.user.UserInstance

interface SettingsRepository {
    suspend fun changePassword(
        user: UserInstance,
        newPassword: String
    ): ChangePasswordState

    suspend fun reAuthenticate(currentUserEmail: String, currentPassword: String): Boolean
    suspend fun generateSecretFor2FA(): String
    suspend fun copy(data: String)
    suspend fun enableOTP(
        userId: String,
        secret: String,
        otpToVerify: String
    ): TwoFAResponse

    suspend fun verifyOTP(
        userId: String,
        otpToVerify: String
    ): TwoFAResponse

    suspend fun updateTwoFAEnabledFlagForUser(
        userId: String,
        twoFAEnabled: Boolean
    ): Boolean

    suspend fun disable2FA(userId: String): TwoFAResponse
    suspend fun verifyBackupCode(
        userId: String,
        backupCode: String
    ): TwoFAResponse

    suspend fun updateVerify2FASuccess()
    suspend fun get2FAVerifiedStatus() : Boolean
    suspend fun delete2FAStatusInLocal()
    suspend fun fetchLoginHistoryList(userId: String): List<SessionItem>
    suspend fun updateUserTimestamp(userId: String, fieldPath: String, value: Long): Boolean
}