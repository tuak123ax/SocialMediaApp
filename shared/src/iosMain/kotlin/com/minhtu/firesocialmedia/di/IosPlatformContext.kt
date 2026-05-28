package com.minhtu.firesocialmedia.di

import com.minhtu.firesocialmedia.core.connectivity.IosNetworkMonitor
import com.minhtu.firesocialmedia.data.local.service.crypto.CryptoService
import com.minhtu.firesocialmedia.data.local.service.room.RoomService
import com.minhtu.firesocialmedia.data.remote.service.auth.AuthService
import com.minhtu.firesocialmedia.data.remote.service.call.AudioCallService
import com.minhtu.firesocialmedia.data.remote.service.clipboard.ClipboardService
import com.minhtu.firesocialmedia.data.remote.service.database.DatabaseService
import com.minhtu.firesocialmedia.data.remote.service.permission.PermissionManager
import com.minhtu.firesocialmedia.core.domain.core.NetworkMonitor
import com.minhtu.firesocialmedia.domain.serviceimpl.auth.IosAuthService
import com.minhtu.firesocialmedia.domain.serviceimpl.call.IosAudioCallService
import com.minhtu.firesocialmedia.domain.serviceimpl.clipboard.IosClipboardService
import com.minhtu.firesocialmedia.domain.serviceimpl.crypto.IosCryptoService
import com.minhtu.firesocialmedia.domain.serviceimpl.database.IosDatabaseService
import com.minhtu.firesocialmedia.domain.serviceimpl.database.supabase.SupabaseStorageHelper
import com.minhtu.firesocialmedia.domain.serviceimpl.permission.IosPermissionManager
import com.minhtu.firesocialmedia.domain.serviceimpl.room.IosRoomService
import com.minhtu.firesocialmedia.data.remote.service.security.IpInfoRemoteDataSource
import com.minhtu.firesocialmedia.platform.AppConfig

open class IosPlatformContext() : PlatformContext {

    init {
        SupabaseStorageHelper.initExtensionCache()
    }

    override val auth: AuthService = IosAuthService()
    override val crypto: CryptoService = IosCryptoService()
    override val database: DatabaseService = IosDatabaseService()
    override val clipboard : ClipboardService = IosClipboardService()
    override val audioCall: AudioCallService = IosAudioCallService()
    override val room: RoomService = IosRoomService()
    override val permissionManager: PermissionManager = IosPermissionManager()
    override val networkMonitor: NetworkMonitor = IosNetworkMonitor()
    override val ipRemoteDataSource: IpInfoRemoteDataSource = IpInfoRemoteDataSource(
        createHttpClient(),
        AppConfig.ipInfoApiKey
    )
}
