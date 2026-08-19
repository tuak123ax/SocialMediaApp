package com.minhtu.firesocialmedia.data.local.service.crypto

/**
 * feature/security's own local clone of core's former `CryptoService` contract, trimmed to only
 * the methods this feature needs (2FA status + account clearing). Backed by core's shared
 * `AndroidCryptoHelper`/`IosCryptoHelper` encryption engine, which every feature already depends
 * on via `core` — no cross-feature Gradle dependency needed.
 */
interface SecurityCryptoService {
    suspend fun clearAccount()
    suspend fun save2FAStatus(status: Boolean)
    suspend fun get2FAStatus(): Boolean
    suspend fun delete2FAStatus()
}
