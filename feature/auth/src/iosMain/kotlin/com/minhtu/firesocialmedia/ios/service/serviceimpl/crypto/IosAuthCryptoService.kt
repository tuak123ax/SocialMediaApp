package com.minhtu.firesocialmedia.ios.service.serviceimpl.crypto

import com.minhtu.firesocialmedia.constants.auth.Constants
import com.minhtu.firesocialmedia.data.local.service.crypto.AuthCryptoService
import com.minhtu.firesocialmedia.data.remote.dto.crypto.CredentialsDTO

class IosAuthCryptoService : AuthCryptoService {
    override fun saveAccount(email: String, password: String) {
        IosCryptoHelper.saveAccount(email, password)
    }

    override suspend fun loadAccount(): CredentialsDTO? {
        val email = IosCryptoHelper.getEmail()
        val password = IosCryptoHelper.getPassword()
        return if (!email.isNullOrEmpty() && !password.isNullOrEmpty()) {
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
}
