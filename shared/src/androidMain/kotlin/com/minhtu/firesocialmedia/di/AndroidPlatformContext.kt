package com.minhtu.firesocialmedia.di

import android.content.Context
import com.minhtu.firesocialmedia.domain.serviceimpl.AuthService
import com.minhtu.firesocialmedia.domain.serviceimpl.auth.AndroidAuthService
import com.minhtu.firesocialmedia.domain.serviceimpl.call.AndroidAudioCallService
import com.minhtu.firesocialmedia.domain.service.call.AudioCallService
import com.minhtu.firesocialmedia.domain.serviceimpl.clipboard.AndroidClipboardService
import com.minhtu.firesocialmedia.domain.service.clipboard.ClipboardService
import com.minhtu.firesocialmedia.domain.serviceimpl.crypto.AndroidCryptoService
import com.minhtu.firesocialmedia.domain.service.crypto.CryptoService
import com.minhtu.firesocialmedia.domain.serviceimpl.database.AndroidDatabaseService
import com.minhtu.firesocialmedia.domain.service.database.DatabaseService
import com.minhtu.firesocialmedia.domain.service.permission.PermissionManager

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