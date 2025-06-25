package com.minhtu.firesocialmedia.di

import android.content.Context
import com.minhtu.firesocialmedia.services.auth.AndroidAuthService
import com.minhtu.firesocialmedia.services.clipboard.AndroidClipboardService
import com.minhtu.firesocialmedia.services.crypto.AndroidCryptoService
import com.minhtu.firesocialmedia.services.database.AndroidDatabaseService
import com.minhtu.firesocialmedia.services.firebase.AndroidFirebaseService

actual class PlatformContext(
    context: Context
) {
    actual val auth: AuthService = AndroidAuthService(context)
    actual val firebase: FirebaseService = AndroidFirebaseService()
    actual val crypto: CryptoService = AndroidCryptoService(context)
    actual val database: DatabaseService = AndroidDatabaseService(context)
    actual val clipboard : ClipboardService = AndroidClipboardService(context)
}

private lateinit var appContext: Context
fun initPlatformContext(context: Context) {
    appContext = context.applicationContext
}