package com.minhtu.firesocialmedia.data.repository

import com.minhtu.firesocialmedia.data.local.service.crypto.SecurityCryptoService
import com.minhtu.firesocialmedia.constants.security.DataConstant
import com.minhtu.firesocialmedia.data.remote.service.auth.SecurityAuthService
import com.minhtu.firesocialmedia.data.remote.service.database.SecurityDatabaseService
import com.minhtu.firesocialmedia.domain.repository.SettingsRepository

class SettingsRepositoryImpl(
    private val authService: SecurityAuthService,
    private val securityDatabaseService: SecurityDatabaseService,
    private val cryptoService: SecurityCryptoService
) : SettingsRepository {
    override suspend fun reAuthenticate(
        currentUserEmail: String,
        currentPassword: String
    ): Boolean {
        return authService.reAuthenticate(currentUserEmail, currentPassword)
    }

    override suspend fun updateVerify2FASuccess() {
        return cryptoService.save2FAStatus(true)
    }

    override suspend fun get2FAVerifiedStatus(): Boolean {
        return cryptoService.get2FAStatus()
    }

    override suspend fun delete2FAStatusInLocal() {
        cryptoService.delete2FAStatus()
    }

    override fun observeSessionStatus(userId: String, sessionId: String, onLoggedOut: () -> Unit) {
        securityDatabaseService.observeSessionStatus(
            userId,
            sessionId,
            DataConstant.HISTORY_PATH,
            DataConstant.LOGIN_PATH,
            onLoggedOut
        )
    }

    override fun stopObserveSessionStatus() {
        securityDatabaseService.stopObserveSessionStatus()
    }
}
