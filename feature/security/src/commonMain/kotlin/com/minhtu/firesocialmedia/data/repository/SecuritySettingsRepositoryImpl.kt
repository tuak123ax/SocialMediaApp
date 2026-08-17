package com.minhtu.firesocialmedia.data.repository

import com.minhtu.firesocialmedia.domain.entity.settings.ChangePasswordState
import com.minhtu.firesocialmedia.security.entity.user.UserInstance
import com.minhtu.firesocialmedia.constants.security.DataConstant
import com.minhtu.firesocialmedia.data.remote.mapper.changepassword.toChangePasswordError
import com.minhtu.firesocialmedia.data.remote.mapper.settings.toDomain
import com.minhtu.firesocialmedia.data.remote.service.auth.SecurityAuthService
import com.minhtu.firesocialmedia.data.remote.service.clipboard.security.ClipboardService
import com.minhtu.firesocialmedia.data.remote.service.database.SecurityDatabaseService
import com.minhtu.firesocialmedia.domain.entity.settings.SessionItem
import com.minhtu.firesocialmedia.domain.repository.SecuritySettingsRepository
import com.minhtu.firesocialmedia.platform.getCurrentTime

class SecuritySettingsRepositoryImpl(
    private val authService: SecurityAuthService,
    private val securityDatabaseService: SecurityDatabaseService,
    private val clipboardService: ClipboardService
) : SecuritySettingsRepository {
    override suspend fun changePassword(
        user: UserInstance,
        newPassword: String
    ): ChangePasswordState {
        val result = authService.changePassword(
            user.uid,
            newPassword,
            DataConstant.USER_PATH,
            DataConstant.LAST_TIME_CHANGE_PASSWORD_PATH
        )
        return result.fold(
            onSuccess = { ChangePasswordState(true) },
            onFailure = { ChangePasswordState(false, it.toChangePasswordError()) }
        )
    }

    override suspend fun copy(data: String) {
        clipboardService.copy(data)
    }

    override suspend fun fetchLoginHistoryList(userId: String): List<SessionItem> {
        val currentSessionId = securityDatabaseService.getLocalSessionId()
        val currentTimeMillis = getCurrentTime()
        return securityDatabaseService.fetchLoginHistoryList(
            userId,
            DataConstant.HISTORY_PATH,
            DataConstant.LOGIN_PATH).map {
            it.toDomain(currentSessionId, currentTimeMillis)
        }
    }

    override suspend fun deleteLoginSession(userId: String, sessionId: String): Boolean {
        return securityDatabaseService.deleteLoginSession(
            userId,
            sessionId,
            DataConstant.HISTORY_PATH,
            DataConstant.LOGIN_PATH
        )
    }

    override suspend fun logoutSession(userId: String, sessionId: String): Boolean {
        return securityDatabaseService.logoutSession(
            userId,
            sessionId,
            DataConstant.HISTORY_PATH,
            DataConstant.LOGIN_PATH
        )
    }

    override suspend fun updateUserTimestamp(userId: String, fieldPath: String, value: Long): Boolean {
        return securityDatabaseService.updateUserLongField(
            userId,
            fieldPath,
            value,
            DataConstant.USER_PATH
        )
    }

    override fun clearLocalSession() {
        securityDatabaseService.clearLocalSessionId()
    }
}
