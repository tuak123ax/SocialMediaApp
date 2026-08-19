package com.minhtu.firesocialmedia.di

import com.minhtu.firesocialmedia.data.local.service.room.NotificationRoomService
import com.minhtu.firesocialmedia.data.remote.service.auth.notification.AuthSessionService
import com.minhtu.firesocialmedia.data.remote.service.database.IosNotificationDatabaseService
import com.minhtu.firesocialmedia.data.remote.service.database.IosNotificationRemoteService
import com.minhtu.firesocialmedia.data.remote.service.database.NotificationDatabaseService
import com.minhtu.firesocialmedia.data.remote.service.database.NotificationRemoteService
import com.minhtu.firesocialmedia.ios.service.serviceimpl.auth.notification.IosAuthSessionService
import com.minhtu.firesocialmedia.ios.service.serviceimpl.room.IosNotificationRoomService
import com.minhtu.firesocialmedia.network.notification.NetworkMonitor
import com.minhtu.firesocialmedia.core.connectivity.notification.IosNetworkMonitor
import org.koin.dsl.module

fun notificationIosModule() = module {
    single<NotificationRoomService> { IosNotificationRoomService() }
    single<NotificationDatabaseService> { IosNotificationDatabaseService() }
    single<NotificationRemoteService> { IosNotificationRemoteService() }
    single<NetworkMonitor> { IosNetworkMonitor() }
    single<AuthSessionService> { IosAuthSessionService() }
}
