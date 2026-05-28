package com.minhtu.firesocialmedia.core.domain.repository

import com.minhtu.firesocialmedia.core.domain.entity.authentication.TwoFAResponse
import com.minhtu.firesocialmedia.core.domain.entity.settings.ChangePasswordState
import com.minhtu.firesocialmedia.core.domain.entity.settings.PollObject
import com.minhtu.firesocialmedia.core.domain.entity.settings.SessionItem
import com.minhtu.firesocialmedia.core.domain.entity.user.UserInstance

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
    suspend fun deleteLoginSession(userId: String, sessionId: String): Boolean
    suspend fun logoutSession(userId: String, sessionId: String): Boolean
    fun observeSessionStatus(userId: String, sessionId: String, onLoggedOut: () -> Unit)
    fun stopObserveSessionStatus()
    suspend fun updateUserTimestamp(userId: String, fieldPath: String, value: Long): Boolean
    suspend fun updateUserStringField(userId: String, fieldPath: String, value: String): Boolean
    suspend fun updateUserAvatar(userId: String, imageUri: String): Boolean
    suspend fun updateUserBackground(userId: String, imageUri: String): Boolean
    suspend fun createPoll(poll: PollObject, newsId: String, groupId: String): Boolean
}