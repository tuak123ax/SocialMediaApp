package com.minhtu.firesocialmedia.domain.service.permission

interface PermissionManager {
    suspend fun requestCameraAndAudioPermissions(): Boolean
    suspend fun requestAudioPermission(): Boolean
}