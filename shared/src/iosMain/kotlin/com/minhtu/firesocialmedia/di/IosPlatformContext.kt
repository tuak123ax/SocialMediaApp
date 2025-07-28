package com.minhtu.firesocialmedia.di

import com.minhtu.firesocialmedia.domain.auth.IosAuthService
import com.minhtu.firesocialmedia.domain.clipboard.IosClipboardService
import com.minhtu.firesocialmedia.domain.crypto.IosCryptoService
import com.minhtu.firesocialmedia.domain.database.IosDatabaseService
import com.minhtu.firesocialmedia.domain.firebase.IosFirebaseService

open class IosPlatformContext(
) : PlatformContext {
    override val auth: AuthService = IosAuthService()
    override val firebase: FirebaseService = IosFirebaseService()
    override val crypto: CryptoService = IosCryptoService()
    override val database: DatabaseService = IosDatabaseService()
    override val clipboard : ClipboardService = IosClipboardService()
}

