package com.minhtu.firesocialmedia.di

import com.minhtu.firesocialmedia.services.auth.IosAuthService
import com.minhtu.firesocialmedia.services.clipboard.IosClipboardService
import com.minhtu.firesocialmedia.services.crypto.IosCryptoService
import com.minhtu.firesocialmedia.services.database.IosDatabaseService
import com.minhtu.firesocialmedia.services.firebase.IosFirebaseService

open class IosPlatformContext(
) : PlatformContext {
    override val auth: AuthService = IosAuthService()
    override val firebase: FirebaseService = IosFirebaseService()
    override val crypto: CryptoService = IosCryptoService()
    override val database: DatabaseService = IosDatabaseService()
    override val clipboard : ClipboardService = IosClipboardService()
}

