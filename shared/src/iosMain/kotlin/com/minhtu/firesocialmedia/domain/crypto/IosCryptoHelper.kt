package com.minhtu.firesocialmedia.domain.crypto

import com.minhtu.firesocialmedia.platform.settings
import kotlinx.cinterop.BetaInteropApi

object IosCryptoHelper {

    private const val ACCOUNT_EMAIL = "KEY_EMAIL"
    private const val ACCOUNT_PASSWORD = "KEY_PASSWORD"

    fun saveAccount(email: String, password: String) {
        saveToKeychain(ACCOUNT_EMAIL, email)
        saveToKeychain(ACCOUNT_PASSWORD, password)
    }

    fun clearAccount() {
        deleteFromKeychain(ACCOUNT_EMAIL)
        deleteFromKeychain(ACCOUNT_PASSWORD)
    }

    fun getEmail(): String? = getFromKeychain(ACCOUNT_EMAIL)

    fun getPassword(): String? = getFromKeychain(ACCOUNT_PASSWORD)

    fun saveToKeychain(key: String, value: String) {
        deleteFromKeychain(key)
        settings?.putString(key, value)
    }

    @OptIn(BetaInteropApi::class)
    fun getFromKeychain(key: String): String?{
        return settings?.getString(key, "")
    }

    private fun deleteFromKeychain(key: String) {
        settings?.remove(key)
    }
}

