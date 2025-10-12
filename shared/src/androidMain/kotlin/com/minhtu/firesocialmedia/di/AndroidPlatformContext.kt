package com.minhtu.firesocialmedia.di

import android.content.Context
import com.minhtu.firesocialmedia.data.remote.service.auth.AuthService
import com.minhtu.firesocialmedia.data.remote.service.call.AudioCallService
import com.minhtu.firesocialmedia.data.remote.service.clipboard.ClipboardService
import com.minhtu.firesocialmedia.data.local.service.crypto.CryptoService
import com.minhtu.firesocialmedia.data.local.service.room.RoomService
import com.minhtu.firesocialmedia.data.remote.service.database.DatabaseService
import com.minhtu.firesocialmedia.data.remote.service.permission.PermissionManager
import com.minhtu.firesocialmedia.domain.serviceimpl.auth.AndroidAuthService
import com.minhtu.firesocialmedia.domain.serviceimpl.call.AndroidAudioCallService
import com.minhtu.firesocialmedia.domain.serviceimpl.clipboard.AndroidClipboardService
import com.minhtu.firesocialmedia.domain.serviceimpl.crypto.AndroidCryptoService
import com.minhtu.firesocialmedia.domain.serviceimpl.database.AndroidDatabaseService
import com.minhtu.firesocialmedia.domain.serviceimpl.room.AndroidRoomService

class AndroidPlatformContext(
    context : Context,
    override val permissionManager: PermissionManager
) : PlatformContext {
    override val auth: AuthService = AndroidAuthService(context)
    override val crypto: CryptoService = AndroidCryptoService(context)
    override val database: DatabaseService = AndroidDatabaseService(context)
    override val clipboard : ClipboardService = AndroidClipboardService(context)
    override val audioCall: AudioCallService = AndroidAudioCallService(context)
    override val room: RoomService = AndroidRoomService(context)
}