package com.minhtu.firesocialmedia.di

import com.minhtu.firesocialmedia.core.connectivity.IosNetworkMonitor
import com.minhtu.firesocialmedia.network.NetworkMonitor
import com.minhtu.firesocialmedia.ios.service.serviceimpl.database.supabase.SupabaseStorageHelper

/**
 * Moved from core to appInit in Phase 3 (Room split). Never built a real Room database (iOS Room
 * impls remain no-op stubs, per existing convention) and no longer has a `room` field at all.
 */
open class IosPlatformContext() : PlatformContext {

    init {
        SupabaseStorageHelper.initExtensionCache()
    }

    override val networkMonitor: NetworkMonitor = IosNetworkMonitor()
}
