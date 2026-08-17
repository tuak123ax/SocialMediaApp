package com.minhtu.firesocialmedia.ios.service.serviceimpl.crypto

import com.minhtu.firesocialmedia.data.local.service.crypto.SecurityCryptoService

class IosSecurityCryptoService : SecurityCryptoService {
    override suspend fun clearAccount() {
        IosCryptoHelper.clearAccount()
    }

    override suspend fun save2FAStatus(status: Boolean) {
        IosCryptoHelper.saveToKeychain("two_fa_status", status.toString())
    }

    override suspend fun get2FAStatus(): Boolean {
        return IosCryptoHelper.getFromKeychain("two_fa_status")?.toBoolean() ?: false
    }

    override suspend fun delete2FAStatus() {
        IosCryptoHelper.saveToKeychain("two_fa_status", "")
    }
}
