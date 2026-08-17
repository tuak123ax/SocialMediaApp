package com.minhtu.firesocialmedia.di

import com.minhtu.firesocialmedia.android.service.serviceimpl.auth.group.AndroidAuthSessionService
import com.minhtu.firesocialmedia.data.remote.service.auth.group.AuthSessionService
import com.minhtu.firesocialmedia.data.remote.service.database.AndroidGroupDatabaseService
import com.minhtu.firesocialmedia.data.remote.service.database.GroupDatabaseService
import com.minhtu.firesocialmedia.data.remote.service.database.GroupStorageHelper
import com.minhtu.firesocialmedia.data.remote.service.database.supabase.GroupSupabaseStorageHelper
import com.minhtu.firesocialmedia.data.remote.service.clipboard.group.ClipboardService
import com.minhtu.firesocialmedia.android.service.serviceimpl.clipboard.group.AndroidClipboardService
import com.minhtu.firesocialmedia.network.group.NetworkMonitor
import com.minhtu.firesocialmedia.core.connectivity.group.NetworkMonitorImpl
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

fun groupAndroidModule() = module {
    single<GroupStorageHelper> { GroupSupabaseStorageHelper() }
    single<GroupDatabaseService> { AndroidGroupDatabaseService() }
    single<ClipboardService> { AndroidClipboardService(androidContext()) }
    single<NetworkMonitor> { NetworkMonitorImpl(androidContext()) }
    single<AuthSessionService> { AndroidAuthSessionService() }
}
