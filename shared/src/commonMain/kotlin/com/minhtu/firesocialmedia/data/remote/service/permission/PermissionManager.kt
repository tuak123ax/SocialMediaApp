package com.minhtu.firesocialmedia.data.remote.service.permission

interface PermissionManager {
    suspend fun requestCameraAndAudioPermissions(): Boolean
    suspend fun requestAudioPermission(): Boolean
}