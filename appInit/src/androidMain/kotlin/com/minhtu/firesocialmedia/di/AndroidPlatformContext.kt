package com.minhtu.firesocialmedia.di

import android.content.Context
import com.minhtu.firesocialmedia.core.connectivity.NetworkMonitorImpl
import com.minhtu.firesocialmedia.network.NetworkMonitor

/**
 * Moved from core to appInit in Phase 3 (Room split). No longer builds or exposes a Room
 * database/Dao instances directly: [AppDatabase][com.minhtu.firesocialmedia.data.local.room.AppDatabase]
 * and its Daos are now constructed and registered as bare Koin singletons by
 * `AppInitAndroidModule.kt`, which each feature's own Koin module (e.g. HomeAndroidModule)
 * consumes via `get()` to build its split RoomService. PlatformContext itself no longer has a
 * `room` field at all (see instruction.md Phase 3).
 */
class AndroidPlatformContext(
    context : Context
) : PlatformContext {
    override val networkMonitor: NetworkMonitor = NetworkMonitorImpl(context)
}
