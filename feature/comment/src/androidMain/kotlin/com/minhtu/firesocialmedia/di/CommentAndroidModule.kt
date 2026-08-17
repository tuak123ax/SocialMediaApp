package com.minhtu.firesocialmedia.di

import com.minhtu.firesocialmedia.android.service.serviceimpl.room.AndroidCommentRoomService
import com.minhtu.firesocialmedia.data.local.dao.CommentDao
import com.minhtu.firesocialmedia.data.local.service.room.CommentRoomService
import com.minhtu.firesocialmedia.data.remote.service.database.AndroidCommentDatabaseService
import com.minhtu.firesocialmedia.data.remote.service.database.AndroidCommentStorageService
import com.minhtu.firesocialmedia.data.remote.service.database.CommentDatabaseService
import com.minhtu.firesocialmedia.data.remote.service.database.CommentStorageService
import com.minhtu.firesocialmedia.data.remote.service.clipboard.comment.ClipboardService
import com.minhtu.firesocialmedia.android.service.serviceimpl.clipboard.comment.AndroidClipboardService
import com.minhtu.firesocialmedia.network.comment.NetworkMonitor
import com.minhtu.firesocialmedia.core.connectivity.comment.NetworkMonitorImpl
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

fun commentAndroidModule() = module {
    single<CommentStorageService> { AndroidCommentStorageService() }
    single<CommentRoomService> { AndroidCommentRoomService(get<CommentDao>()) }
    single<CommentDatabaseService> { AndroidCommentDatabaseService() }
    single<ClipboardService> { AndroidClipboardService(androidContext()) }
    single<NetworkMonitor> { NetworkMonitorImpl(androidContext()) }
}
