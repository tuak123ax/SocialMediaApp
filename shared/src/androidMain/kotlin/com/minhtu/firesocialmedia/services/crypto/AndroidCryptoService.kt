package com.minhtu.firesocialmedia.services.crypto

import android.content.Context
import com.minhtu.firesocialmedia.Credentials
import com.minhtu.firesocialmedia.CryptoService
import com.minhtu.firesocialmedia.constants.Constants

class AndroidCryptoService(private val context: Context) : CryptoService{
    override fun saveAccount(email: String, password: String) {
        AndroidCryptoHelper.saveAccount(context, email, password)
    }

    override suspend fun loadAccount(): Credentials? {
        val secureSharedPreferences = AndroidCryptoHelper.getEncryptedSharedPreferences(context)
        val email = secureSharedPreferences.getString(Constants.KEY_EMAIL, "")
        val password = secureSharedPreferences.getString(Constants.KEY_PASSWORD, "")
        return if(!email.isNullOrEmpty() && !password.isNullOrEmpty()){
            Credentials(email, password)
        } else {
            null
        }
    }

    override fun clearAccount() {
        AndroidCryptoHelper.clearAccount(context)
    }

    override suspend fun getFCMToken(): String {
        val secureSharedPreferences = AndroidCryptoHelper.getEncryptedSharedPreferences(context)
        return secureSharedPreferences.getString(Constants.KEY_FCM_TOKEN, "")!!
    }
}