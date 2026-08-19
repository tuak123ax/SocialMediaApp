package com.minhtu.firesocialmedia.data.local.service.crypto

import com.minhtu.firesocialmedia.data.remote.auth.dto.crypto.CredentialsDTO

/**
 * feature/auth's own local clone of core's former `CryptoService` contract, trimmed to only the
 * methods this feature needs (saved login credentials + FCM token). Backed by core's shared
 * `AndroidCryptoHelper`/`IosCryptoHelper` encryption engine, which every feature already depends
 * on via `core` — no cross-feature Gradle dependency needed.
 */
interface AuthCryptoService {
    fun saveAccount(email: String, password: String)
    suspend fun loadAccount(): CredentialsDTO?
    suspend fun clearAccount()
    suspend fun getFCMToken(): String
}
