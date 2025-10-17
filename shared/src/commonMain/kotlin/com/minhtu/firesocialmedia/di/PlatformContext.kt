package com.minhtu.firesocialmedia.di

import com.minhtu.firesocialmedia.data.local.service.crypto.CryptoService
import com.minhtu.firesocialmedia.data.local.service.room.RoomService
import com.minhtu.firesocialmedia.data.remote.service.auth.AuthService
import com.minhtu.firesocialmedia.data.remote.service.call.AudioCallService
import com.minhtu.firesocialmedia.data.remote.service.clipboard.ClipboardService
import com.minhtu.firesocialmedia.data.remote.service.database.DatabaseService
import com.minhtu.firesocialmedia.data.remote.service.permission.PermissionManager
import com.minhtu.firesocialmedia.domain.core.NetworkMonitor
import io.mockative.Mockable

@Mockable
interface PlatformContext {
    val auth: AuthService
    val crypto: CryptoService
    val database : DatabaseService
    val clipboard : ClipboardService
    val audioCall : AudioCallService
    val room : RoomService
    val permissionManager : PermissionManager
    val networkMonitor : NetworkMonitor
}