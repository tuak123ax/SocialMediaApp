package com.minhtu.firesocialmedia.di

import android.content.Context
import androidx.room.Room.databaseBuilder
import com.minhtu.firesocialmedia.core.connectivity.NetworkMonitorImpl
import com.minhtu.firesocialmedia.data.local.room.LocalDatabase
import com.minhtu.firesocialmedia.data.local.room.MIGRATION_7_8
import com.minhtu.firesocialmedia.data.local.room.MIGRATION_8_9
import com.minhtu.firesocialmedia.data.local.service.crypto.CryptoService
import com.minhtu.firesocialmedia.data.local.service.room.RoomService
import com.minhtu.firesocialmedia.data.remote.service.auth.AuthService
import com.minhtu.firesocialmedia.data.remote.service.call.AudioCallService
import com.minhtu.firesocialmedia.data.remote.service.clipboard.ClipboardService
import com.minhtu.firesocialmedia.data.remote.service.database.DatabaseService
import com.minhtu.firesocialmedia.data.remote.service.permission.PermissionManager
import com.minhtu.firesocialmedia.domain.core.NetworkMonitor
import com.minhtu.firesocialmedia.domain.serviceimpl.auth.AndroidAuthService
import com.minhtu.firesocialmedia.domain.serviceimpl.call.AndroidAudioCallService
import com.minhtu.firesocialmedia.domain.serviceimpl.clipboard.AndroidClipboardService
import com.minhtu.firesocialmedia.domain.serviceimpl.crypto.AndroidCryptoService
import com.minhtu.firesocialmedia.domain.serviceimpl.database.AndroidDatabaseService
import com.minhtu.firesocialmedia.domain.serviceimpl.database.supabase.SupabaseStorageHelper
import com.minhtu.firesocialmedia.data.remote.service.security.IpInfoRemoteDataSource
import com.minhtu.firesocialmedia.domain.serviceimpl.room.AndroidRoomService
import com.minhtu.firesocialmedia.platform.AppConfig

class AndroidPlatformContext(
    context : Context,
    override val permissionManager: PermissionManager
) : PlatformContext {
    private val localDatabase : LocalDatabase by lazy {
        databaseBuilder(
            context.applicationContext,
            LocalDatabase::class.java,
            "RoomLocalDatabase"
        ).addMigrations(MIGRATION_7_8, MIGRATION_8_9).build()
    }
    override val auth: AuthService = AndroidAuthService(context)
    override val crypto: CryptoService = AndroidCryptoService(context)
    override val database: DatabaseService = AndroidDatabaseService(context, SupabaseStorageHelper())
    override val clipboard : ClipboardService = AndroidClipboardService(context)
    override val audioCall: AudioCallService by lazy { AndroidAudioCallService.get(context) }
    override val room: RoomService = AndroidRoomService(
        context,
        localDatabase.userDao(),
        localDatabase.newsDao(),
        localDatabase.notificationDao(),
        localDatabase.commentDao()
    )
    override val networkMonitor: NetworkMonitor = NetworkMonitorImpl(context)
    override val ipRemoteDataSource: IpInfoRemoteDataSource = IpInfoRemoteDataSource(
        createHttpClient(),
        AppConfig.ipInfoApiKey
    )
}