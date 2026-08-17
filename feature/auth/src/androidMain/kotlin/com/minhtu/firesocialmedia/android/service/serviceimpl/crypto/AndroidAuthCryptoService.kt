package com.minhtu.firesocialmedia.android.service.serviceimpl.crypto

import android.content.Context
import com.minhtu.firesocialmedia.constants.auth.Constants
import com.minhtu.firesocialmedia.data.local.service.crypto.AuthCryptoService
import com.minhtu.firesocialmedia.data.remote.dto.crypto.CredentialsDTO

class AndroidAuthCryptoService(private val context: Context) : AuthCryptoService {
    override fun saveAccount(email: String, password: String) {
        AndroidCryptoHelper.saveAccount(context, email, password)
    }

    override suspend fun loadAccount(): CredentialsDTO? {
        val secureSharedPreferences = AndroidCryptoHelper.getEncryptedSharedPreferences(context)
        val email = secureSharedPreferences.getString(Constants.KEY_EMAIL, "")
        val password = secureSharedPreferences.getString(Constants.KEY_PASSWORD, "")
        return if (!email.isNullOrEmpty() && !password.isNullOrEmpty()) {
            CredentialsDTO(email, password)
        } else {
            null
        }
    }

    override suspend fun clearAccount() {
        AndroidCryptoHelper.clearAccount(context)
    }

    override suspend fun getFCMToken(): String {
        val secureSharedPreferences = AndroidCryptoHelper.getEncryptedSharedPreferences(context)
        return secureSharedPreferences.getString(Constants.KEY_FCM_TOKEN, "")!!
    }
}
