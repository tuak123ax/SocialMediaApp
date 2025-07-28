package com.minhtu.firesocialmedia.domain

interface PermissionManager {
    suspend fun requestCameraAndAudioPermissions(): Boolean
    suspend fun requestAudioPermission(): Boolean
}