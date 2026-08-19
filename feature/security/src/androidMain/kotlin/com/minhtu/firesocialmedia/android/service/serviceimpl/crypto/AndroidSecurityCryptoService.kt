package com.minhtu.firesocialmedia.android.service.serviceimpl.crypto

import android.content.Context
import com.minhtu.firesocialmedia.data.local.service.crypto.SecurityCryptoService

class AndroidSecurityCryptoService(private val context: Context) : SecurityCryptoService {
    override suspend fun clearAccount() {
        AndroidCryptoHelper.clearAccount(context)
    }

    override suspend fun save2FAStatus(status: Boolean) {
        AndroidCryptoHelper.save2FAStatus(context, status)
    }

    override suspend fun get2FAStatus(): Boolean {
        return AndroidCryptoHelper.get2FAStatus(context)
    }

    override suspend fun delete2FAStatus() {
        AndroidCryptoHelper.delete2FAStatus(context)
    }
}
