package com.minhtu.firesocialmedia.di

import com.minhtu.firesocialmedia.data.remote.service.call.AudioCallService
import com.minhtu.firesocialmedia.data.remote.service.database.CallDatabaseService
import com.minhtu.firesocialmedia.data.remote.service.database.IosCallDatabaseService
import com.minhtu.firesocialmedia.data.remote.service.permission.PermissionManager
import com.minhtu.firesocialmedia.ios.service.serviceimpl.call.IosAudioCallService
import com.minhtu.firesocialmedia.ios.service.serviceimpl.permission.IosPermissionManager
import org.koin.dsl.module

fun callingIosModule() = module {
    single<AudioCallService> { IosAudioCallService() }
    single<PermissionManager> { IosPermissionManager() }
    single<CallDatabaseService> { IosCallDatabaseService() }
}
