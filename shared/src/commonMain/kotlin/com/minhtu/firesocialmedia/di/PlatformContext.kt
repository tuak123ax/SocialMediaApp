package com.minhtu.firesocialmedia.di

import com.minhtu.firesocialmedia.domain.serviceimpl.AuthService
import com.minhtu.firesocialmedia.domain.service.call.AudioCallService
import com.minhtu.firesocialmedia.domain.service.clipboard.ClipboardService
import com.minhtu.firesocialmedia.domain.service.crypto.CryptoService
import com.minhtu.firesocialmedia.domain.service.database.DatabaseService
import com.minhtu.firesocialmedia.domain.service.permission.PermissionManager
import io.mockative.Mockable

@Mockable
interface PlatformContext {
    val auth: AuthService
    val crypto: CryptoService
    val database : DatabaseService
    val clipboard : ClipboardService
    val audioCall : AudioCallService
    val permissionManager : PermissionManager
}