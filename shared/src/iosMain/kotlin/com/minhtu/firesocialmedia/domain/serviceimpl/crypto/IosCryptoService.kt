package com.minhtu.firesocialmedia.domain.serviceimpl.crypto

import com.minhtu.firesocialmedia.constants.Constants
import com.minhtu.firesocialmedia.data.dto.crypto.CredentialsDTO
import com.minhtu.firesocialmedia.domain.service.crypto.CryptoService

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
}