package com.minhtu.firesocialmedia.di

import com.minhtu.firesocialmedia.android.service.serviceimpl.auth.notification.AndroidAuthSessionService
import com.minhtu.firesocialmedia.android.service.serviceimpl.room.AndroidNotificationRoomService
import com.minhtu.firesocialmedia.data.local.dao.NotificationDao
import com.minhtu.firesocialmedia.data.local.service.room.NotificationRoomService
import com.minhtu.firesocialmedia.data.remote.service.auth.notification.AuthSessionService
import com.minhtu.firesocialmedia.data.remote.service.database.AndroidNotificationDatabaseService
import com.minhtu.firesocialmedia.data.remote.service.database.AndroidNotificationRemoteService
import com.minhtu.firesocialmedia.data.remote.service.database.NotificationDatabaseService
import com.minhtu.firesocialmedia.data.remote.service.database.NotificationRemoteService
import com.minhtu.firesocialmedia.network.notification.NetworkMonitor
import com.minhtu.firesocialmedia.core.connectivity.notification.NetworkMonitorImpl
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

fun notificationAndroidModule() = module {
    single<NotificationRoomService> { AndroidNotificationRoomService(get<NotificationDao>()) }
    single<NotificationDatabaseService> { AndroidNotificationDatabaseService() }
    single<NotificationRemoteService> { AndroidNotificationRemoteService() }
    single<NetworkMonitor> { NetworkMonitorImpl(androidContext()) }
    single<AuthSessionService> { AndroidAuthSessionService() }
}
