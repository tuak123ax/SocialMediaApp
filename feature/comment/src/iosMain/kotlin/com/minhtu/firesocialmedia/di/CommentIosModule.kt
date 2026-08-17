package com.minhtu.firesocialmedia.di

import com.minhtu.firesocialmedia.data.local.service.room.CommentRoomService
import com.minhtu.firesocialmedia.data.remote.service.database.CommentDatabaseService
import com.minhtu.firesocialmedia.data.remote.service.database.CommentStorageService
import com.minhtu.firesocialmedia.data.remote.service.database.IosCommentDatabaseService
import com.minhtu.firesocialmedia.data.remote.service.database.IosCommentStorageService
import com.minhtu.firesocialmedia.ios.service.serviceimpl.room.IosCommentRoomService
import com.minhtu.firesocialmedia.data.remote.service.clipboard.comment.ClipboardService
import com.minhtu.firesocialmedia.ios.service.serviceimpl.clipboard.comment.IosClipboardService
import com.minhtu.firesocialmedia.network.comment.NetworkMonitor
import com.minhtu.firesocialmedia.core.connectivity.comment.IosNetworkMonitor
import org.koin.dsl.module

fun commentIosModule() = module {
    single<CommentStorageService> { IosCommentStorageService() }
    single<CommentRoomService> { IosCommentRoomService() }
    single<CommentDatabaseService> { IosCommentDatabaseService() }
    single<ClipboardService> { IosClipboardService() }
    single<NetworkMonitor> { IosNetworkMonitor() }
}
