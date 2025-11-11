package com.minhtu.firesocialmedia.domain.serviceimpl.crypto

import com.minhtu.firesocialmedia.constants.Constants
import com.minhtu.firesocialmedia.data.local.service.crypto.CryptoService
import com.minhtu.firesocialmedia.data.remote.dto.crypto.CredentialsDTO
import com.minhtu.firesocialmedia.data.remote.dto.user.UserDTO
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class IosCryptoService() : CryptoService {
    override fun saveAccount(email: String, password: String) {
        IosCryptoHelper.saveAccount(email, password)
    }

    override suspend fun loadAccount(): CredentialsDTO? {
        val email = IosCryptoHelper.getEmail()
        val password = IosCryptoHelper.getPassword()
        return if(!email.isNullOrEmpty() && !password.isNullOrEmpty()){
            CredentialsDTO(email, password)
        } else {
            null
        }
    }

    override suspend fun clearAccount() {
        IosCryptoHelper.clearAccount()
    }

    override suspend fun getFCMToken(): String {
        return IosCryptoHelper.getFromKeychain(Constants.KEY_FCM_TOKEN) ?: ""
    }

    override suspend fun saveCurrentUserInfo(user: UserDTO) {
        val json = Json.encodeToString(user)
        IosCryptoHelper.saveToKeychain("current_user_info", json)
    }

    override suspend fun getCurrentUserInfo(): UserDTO? {
        val json = IosCryptoHelper.getFromKeychain("current_user_info")
        return json?.takeIf { it.isNotEmpty() }?.let {
            runCatching { Json.decodeFromString<UserDTO>(it) }.getOrNull()
        }
    }
}