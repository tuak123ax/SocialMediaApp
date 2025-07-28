package com.minhtu.firesocialmedia.di

import com.minhtu.firesocialmedia.domain.AudioCallService
import com.minhtu.firesocialmedia.domain.AuthService
import com.minhtu.firesocialmedia.domain.ClipboardService
import com.minhtu.firesocialmedia.domain.CryptoService
import com.minhtu.firesocialmedia.domain.DatabaseService
import com.minhtu.firesocialmedia.domain.PermissionManager
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