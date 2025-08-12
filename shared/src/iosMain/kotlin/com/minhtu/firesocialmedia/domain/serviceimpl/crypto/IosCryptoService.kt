package com.minhtu.firesocialmedia.domain.serviceimpl.crypto

import com.minhtu.firesocialmedia.constants.Constants
import com.minhtu.firesocialmedia.data.model.crypto.Credentials

class IosCryptoService() : CryptoService{
    override fun saveAccount(email: String, password: String) {
        IosCryptoHelper.saveAccount(email, password)
    }

    override suspend fun loadAccount(): Credentials? {
        val email = IosCryptoHelper.getEmail()
        val password = IosCryptoHelper.getPassword()
        return if(!email.isNullOrEmpty() && !password.isNullOrEmpty()){
            Credentials(email, password)
        } else {
            null
        }
    }

    override fun clearAccount() {
        IosCryptoHelper.clearAccount()
    }

    override suspend fun getFCMToken(): String {
        return IosCryptoHelper.getFromKeychain(Constants.KEY_FCM_TOKEN) ?: ""
    }
}