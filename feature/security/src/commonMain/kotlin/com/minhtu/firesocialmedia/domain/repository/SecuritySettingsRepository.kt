package com.minhtu.firesocialmedia.domain.repository

import com.minhtu.firesocialmedia.domain.entity.settings.ChangePasswordState
import com.minhtu.firesocialmedia.security.entity.user.UserInstance
import com.minhtu.firesocialmedia.domain.entity.settings.SessionItem

interface SecuritySettingsRepository {
    suspend fun changePassword(
        user: UserInstance,
        newPassword: String
    ): ChangePasswordState

    suspend fun copy(data: String)
    suspend fun fetchLoginHistoryList(userId: String): List<SessionItem>
    suspend fun deleteLoginSession(userId: String, sessionId: String): Boolean
    suspend fun logoutSession(userId: String, sessionId: String): Boolean
    suspend fun updateUserTimestamp(userId: String, fieldPath: String, value: Long): Boolean
    fun clearLocalSession()
}
