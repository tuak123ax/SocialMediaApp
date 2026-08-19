package com.minhtu.firesocialmedia.data.remote.service.database

import com.minhtu.firesocialmedia.data.remote.dto.settings.security.SessionItemDTO

/**
 * Feature-security-owned counterpart of core's generic `DatabaseService`, scoped to login-session
 * (login-history / device-session) operations only (Firebase Realtime Database + local session-id
 * storage). This mirrors feature/home's `HomeDatabaseService` pattern: keeps core's `DatabaseService`
 * free of any Session-typed methods while feature/security retains direct access to the underlying
 * data source.
 */
interface SecurityDatabaseService {
    suspend fun fetchLoginHistoryList(
        userId: String,
        historyPath: String,
        loginHistoryPath: String
    ): List<SessionItemDTO>

    fun getLocalSessionId(): String

    fun clearLocalSessionId()

    fun observeSessionStatus(
        userId: String,
        sessionId: String,
        historyPath: String,
        loginHistoryPath: String,
        onLoggedOut: () -> Unit
    )

    fun stopObserveSessionStatus()

    suspend fun deleteLoginSession(
        userId: String,
        sessionId: String,
        historyPath: String,
        loginHistoryPath: String
    ): Boolean

    suspend fun logoutSession(
        userId: String,
        sessionId: String,
        historyPath: String,
        loginHistoryPath: String
    ): Boolean

    suspend fun downloadImage(image: String, fileName: String): Boolean

    suspend fun updateTwoFAEnabledFlagForUser(
        userId: String,
        twoFAEnabled: Boolean,
        userPath: String,
        twoFaEnabledPath: String
    ): Boolean

    suspend fun updateUserLongField(
        userId: String,
        fieldPath: String,
        value: Long,
        userPath: String
    ): Boolean
}
