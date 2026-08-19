package com.minhtu.firesocialmedia.domain.repository

interface SettingsRepository {
    suspend fun reAuthenticate(currentUserEmail: String, currentPassword: String): Boolean

    suspend fun updateVerify2FASuccess()
    suspend fun get2FAVerifiedStatus() : Boolean
    suspend fun delete2FAStatusInLocal()
    fun observeSessionStatus(userId: String, sessionId: String, onLoggedOut: () -> Unit)
    fun stopObserveSessionStatus()
}
