package com.minhtu.firesocialmedia.ios.service.serviceimpl.crypto

import com.minhtu.firesocialmedia.data.local.service.crypto.ProfileCryptoService
import com.minhtu.firesocialmedia.profile.data.remote.dto.user.UserDTO
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json

class IosProfileCryptoService : ProfileCryptoService {
    override suspend fun getCurrentUserInfo(): UserDTO? {
        val json = IosCryptoHelper.getFromKeychain("current_user_info")
        return json?.takeIf { it.isNotEmpty() }?.let {
            runCatching { Json.decodeFromString<UserDTO>(it) }.getOrNull()
        }
    }
}
