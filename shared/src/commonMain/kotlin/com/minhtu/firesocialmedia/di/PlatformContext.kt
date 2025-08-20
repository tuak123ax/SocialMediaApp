package com.minhtu.firesocialmedia.di

import com.minhtu.firesocialmedia.domain.serviceimpl.AuthService
import com.minhtu.firesocialmedia.domain.serviceimpl.call.AudioCallService
import com.minhtu.firesocialmedia.domain.serviceimpl.clipboard.ClipboardService
import com.minhtu.firesocialmedia.domain.serviceimpl.crypto.CryptoService
import com.minhtu.firesocialmedia.domain.serviceimpl.database.DatabaseService
import com.minhtu.firesocialmedia.domain.serviceimpl.permission.PermissionManager
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