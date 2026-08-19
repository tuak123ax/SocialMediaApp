package com.minhtu.firesocialmedia.di

import com.minhtu.firesocialmedia.data.remote.service.auth.group.AuthSessionService
import com.minhtu.firesocialmedia.data.remote.service.database.GroupDatabaseService
import com.minhtu.firesocialmedia.data.remote.service.database.GroupStorageHelper
import com.minhtu.firesocialmedia.data.remote.service.database.IosGroupDatabaseService
import com.minhtu.firesocialmedia.data.remote.service.database.supabase.GroupSupabaseStorageHelper
import com.minhtu.firesocialmedia.data.remote.service.clipboard.group.ClipboardService
import com.minhtu.firesocialmedia.ios.service.serviceimpl.auth.group.IosAuthSessionService
import com.minhtu.firesocialmedia.ios.service.serviceimpl.clipboard.group.IosClipboardService
import com.minhtu.firesocialmedia.network.group.NetworkMonitor
import com.minhtu.firesocialmedia.core.connectivity.group.IosNetworkMonitor
import org.koin.dsl.module

fun groupIosModule() = module {
    single<GroupStorageHelper> { GroupSupabaseStorageHelper() }
    single<GroupDatabaseService> { IosGroupDatabaseService() }
    single<ClipboardService> { IosClipboardService() }
    single<NetworkMonitor> { IosNetworkMonitor() }
    single<AuthSessionService> { IosAuthSessionService() }
}
