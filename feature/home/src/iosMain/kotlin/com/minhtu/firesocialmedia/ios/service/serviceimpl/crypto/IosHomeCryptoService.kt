package com.minhtu.firesocialmedia.ios.service.serviceimpl.crypto

import com.minhtu.firesocialmedia.data.local.service.crypto.HomeCryptoService
import com.minhtu.firesocialmedia.home.data.remote.dto.user.UserDTO
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class IosHomeCryptoService : HomeCryptoService {
    override suspend fun clearAccount() {
        IosCryptoHelper.clearAccount()
    }

    override suspend fun saveCurrentUserInfo(user: UserDTO) {
        val json = Json.encodeToString(user)
        IosCryptoHelper.saveToKeychain("current_user_info", json)
    }
}
