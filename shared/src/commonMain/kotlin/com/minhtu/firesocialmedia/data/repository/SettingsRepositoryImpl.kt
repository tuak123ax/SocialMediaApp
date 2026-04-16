package com.minhtu.firesocialmedia.data.repository

import com.minhtu.firesocialmedia.data.local.service.crypto.CryptoService
import com.minhtu.firesocialmedia.data.remote.constant.DataConstant
import com.minhtu.firesocialmedia.domain.entity.authentication.TwoFAResponse
import com.minhtu.firesocialmedia.data.remote.mapper.user.toDto
import com.minhtu.firesocialmedia.data.remote.service.auth.AuthService
import com.minhtu.firesocialmedia.data.remote.service.clipboard.ClipboardService
import com.minhtu.firesocialmedia.data.remote.service.database.DatabaseService
import com.minhtu.firesocialmedia.domain.entity.settings.ChangePasswordState
import com.minhtu.firesocialmedia.domain.entity.user.UserInstance
import com.minhtu.firesocialmedia.domain.repository.SettingsRepository

class SettingsRepositoryImpl(
    private val authService: AuthService,
    private val databaseService: DatabaseService,
    private val clipboardService: ClipboardService,
    private val cryptoService: CryptoService
) : SettingsRepository {
    override suspend fun changePassword(
        user: UserInstance,
        newPassword: String
    ): ChangePasswordState {
        return authService.changePassword(
            user.toDto(),
            newPassword,
            DataConstant.USER_PATH,
            DataConstant.LAST_TIME_CHANGE_PASSWORD_PATH
        )
    }

    override suspend fun reAuthenticate(
        currentUserEmail: String,
        currentPassword: String
    ): Boolean {
        return authService.reAuthenticate(currentUserEmail, currentPassword)
    }

    override suspend fun generateSecretFor2FA(): String {
        return authService.generateSecretFor2FA()
    }

    override suspend fun copy(data: String) {
        clipboardService.copy(data)
    }

    override suspend fun enableOTP(
        userId: String,
        secret: String,
        otpToVerify: String
    ): TwoFAResponse {
        return authService.enableOTP(
            userId,
            secret,
            otpToVerify
        )
    }

    override suspend fun verifyOTP(
        userId: String,
        otpToVerify: String
    ): TwoFAResponse {
        return authService.verifyOTP(
            userId,
            otpToVerify
        )
    }

    override suspend fun updateTwoFAEnabledFlagForUser(
        userId: String,
        twoFAEnabled: Boolean
    ): Boolean {
        return databaseService.updateTwoFAEnabledFlagForUser(
            userId,
            twoFAEnabled,
            DataConstant.USER_PATH,
            DataConstant.TWO_FA_ENABLED_PATH
        )
    }

    override suspend fun disable2FA(userId: String): TwoFAResponse {
        return authService.disable2FA(
            userId
        )
    }

    override suspend fun verifyBackupCode(
        userId: String,
        backupCode: String
    ): TwoFAResponse {
        return authService.verifyBackupCode(
            userId,
            backupCode
        )
    }

    override suspend fun updateVerify2FASuccess() {
        return cryptoService.save2FAStatus(true)
    }

    override suspend fun get2FAVerifiedStatus() : Boolean {
        return cryptoService.get2FAStatus()
    }

    override suspend fun delete2FAStatusInLocal() {
        cryptoService.delete2FAStatus()
    }
}