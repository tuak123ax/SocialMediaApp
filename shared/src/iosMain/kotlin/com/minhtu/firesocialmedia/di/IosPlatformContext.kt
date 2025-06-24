package com.minhtu.firesocialmedia.di

import com.minhtu.firesocialmedia.services.auth.IosAuthService
import com.minhtu.firesocialmedia.services.clipboard.IosClipboardService
import com.minhtu.firesocialmedia.services.crypto.IosCryptoService
import com.minhtu.firesocialmedia.services.database.IosDatabaseService
import com.minhtu.firesocialmedia.services.firebase.IosFirebaseService

actual class PlatformContext(
) {
    actual val auth: AuthService = IosAuthService()
    actual val firebase: FirebaseService = IosFirebaseService()
    actual val crypto: CryptoService = IosCryptoService()
    actual val database: DatabaseService = IosDatabaseService()
    actual val clipboard : ClipboardService = IosClipboardService()
}

