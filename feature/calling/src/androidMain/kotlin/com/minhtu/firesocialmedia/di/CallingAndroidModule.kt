package com.minhtu.firesocialmedia.di

import com.minhtu.firesocialmedia.android.service.serviceimpl.call.AndroidAudioCallService
import com.minhtu.firesocialmedia.data.remote.service.call.AudioCallService
import com.minhtu.firesocialmedia.data.remote.service.database.AndroidCallDatabaseService
import com.minhtu.firesocialmedia.data.remote.service.database.CallDatabaseService
import com.minhtu.firesocialmedia.data.remote.service.permission.PermissionManager
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

fun callingAndroidModule() = module {
    single<AudioCallService> { AndroidAudioCallService.get(androidContext()) }
    single<PermissionManager> { PermissionManagerHolder.instance }
    single<CallDatabaseService> { AndroidCallDatabaseService() }
}
