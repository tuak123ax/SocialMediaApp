package com.minhtu.firesocialmedia.data.local.service.crypto

import com.minhtu.firesocialmedia.home.data.remote.dto.user.UserDTO

/**
 * feature/home's own local clone of core's former `CryptoService` contract, trimmed to only the
 * methods this feature needs (clearing the account + caching the current user's info offline).
 * Backed by core's shared `AndroidCryptoHelper`/`IosCryptoHelper` encryption engine, which every
 * feature already depends on via `core` — no cross-feature Gradle dependency needed.
 */
interface HomeCryptoService {
    suspend fun clearAccount()
    suspend fun saveCurrentUserInfo(user: UserDTO)
}
