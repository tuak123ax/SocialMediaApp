package com.minhtu.firesocialmedia.domain.serviceimpl.permission

import com.minhtu.firesocialmedia.domain.service.permission.PermissionManager

class IosPermissionManager : PermissionManager {
    override suspend fun requestCameraAndAudioPermissions(): Boolean {
        // iOS implementation will be added later
        return true
    }

    override suspend fun requestAudioPermission(): Boolean {
        // iOS implementation will be added later
        return true
    }
}
