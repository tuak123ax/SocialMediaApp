package com.minhtu.firesocialmedia.di

import android.content.Context
import com.minhtu.firesocialmedia.domain.AudioCallService
import com.minhtu.firesocialmedia.domain.AuthService
import com.minhtu.firesocialmedia.domain.ClipboardService
import com.minhtu.firesocialmedia.domain.CryptoService
import com.minhtu.firesocialmedia.domain.DatabaseService
import com.minhtu.firesocialmedia.domain.PermissionManager
import com.minhtu.firesocialmedia.domain.call.AndroidAudioCallService
import com.minhtu.firesocialmedia.domain.auth.AndroidAuthService
import com.minhtu.firesocialmedia.domain.clipboard.AndroidClipboardService
import com.minhtu.firesocialmedia.domain.crypto.AndroidCryptoService
import com.minhtu.firesocialmedia.domain.database.AndroidDatabaseService

class AndroidPlatformContext(
    context : Context,
    override val permissionManager: PermissionManager
) : PlatformContext {
    override val auth: AuthService = AndroidAuthService(context)
    override val crypto: CryptoService = AndroidCryptoService(context)
    override val database: DatabaseService = AndroidDatabaseService(context)
    override val clipboard : ClipboardService = AndroidClipboardService(context)
    override val audioCall: AudioCallService = AndroidAudioCallService(context)
}