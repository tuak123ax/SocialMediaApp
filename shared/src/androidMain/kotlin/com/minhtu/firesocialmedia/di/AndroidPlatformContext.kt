package com.minhtu.firesocialmedia.di

import android.content.Context
import com.minhtu.firesocialmedia.platform.PermissionManager
import com.minhtu.firesocialmedia.services.audiocall.AndroidAudioCallService
import com.minhtu.firesocialmedia.services.auth.AndroidAuthService
import com.minhtu.firesocialmedia.services.clipboard.AndroidClipboardService
import com.minhtu.firesocialmedia.services.crypto.AndroidCryptoService
import com.minhtu.firesocialmedia.services.database.AndroidDatabaseService
import com.minhtu.firesocialmedia.services.firebase.AndroidFirebaseService

class AndroidPlatformContext(
    context : Context,
    override val permissionManager: PermissionManager
) : PlatformContext {
    override val auth: AuthService = AndroidAuthService(context)
    override val firebase: FirebaseService = AndroidFirebaseService()
    override val crypto: CryptoService = AndroidCryptoService(context)
    override val database: DatabaseService = AndroidDatabaseService(context)
    override val clipboard : ClipboardService = AndroidClipboardService(context)
    override val audioCall: AudioCallService = AndroidAudioCallService(context)
}