package com.minhtu.firesocialmedia.data.local.service.crypto

import com.minhtu.firesocialmedia.profile.data.remote.dto.user.UserDTO

/**
 * feature/profile's own local clone of core's former `CryptoService` contract, trimmed to only
 * the method this feature needs (reading the cached current-user info when offline). Backed by
 * core's shared `AndroidCryptoHelper`/`IosCryptoHelper` encryption engine, which every feature
 * already depends on via `core` — no cross-feature Gradle dependency needed.
 */
interface ProfileCryptoService {
    suspend fun getCurrentUserInfo(): UserDTO?
}
