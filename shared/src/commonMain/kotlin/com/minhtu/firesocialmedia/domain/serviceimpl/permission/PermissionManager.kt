package com.minhtu.firesocialmedia.domain.serviceimpl.permission

interface PermissionManager {
    suspend fun requestCameraAndAudioPermissions(): Boolean
    suspend fun requestAudioPermission(): Boolean
}