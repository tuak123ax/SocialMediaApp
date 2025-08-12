package com.minhtu.firesocialmedia.di

import com.minhtu.firesocialmedia.domain.serviceimpl.AuthService
import com.minhtu.firesocialmedia.domain.serviceimpl.auth.IosAuthService
import com.minhtu.firesocialmedia.domain.serviceimpl.call.AudioCallService
import com.minhtu.firesocialmedia.domain.serviceimpl.call.IosAudioCallService
import com.minhtu.firesocialmedia.domain.serviceimpl.clipboard.ClipboardService
import com.minhtu.firesocialmedia.domain.serviceimpl.clipboard.IosClipboardService
import com.minhtu.firesocialmedia.domain.serviceimpl.crypto.CryptoService
import com.minhtu.firesocialmedia.domain.serviceimpl.crypto.IosCryptoService
import com.minhtu.firesocialmedia.domain.serviceimpl.database.DatabaseService
import com.minhtu.firesocialmedia.domain.serviceimpl.database.IosDatabaseService
import com.minhtu.firesocialmedia.domain.serviceimpl.permission.IosPermissionManager
import com.minhtu.firesocialmedia.domain.serviceimpl.permission.PermissionManager

open class IosPlatformContext(
) : PlatformContext {
    override val auth: AuthService = IosAuthService()
    override val crypto: CryptoService = IosCryptoService()
    override val database: DatabaseService = IosDatabaseService()
    override val clipboard : ClipboardService = IosClipboardService()
    override val audioCall: AudioCallService = IosAudioCallService()
    override val permissionManager: PermissionManager = IosPermissionManager()
}

