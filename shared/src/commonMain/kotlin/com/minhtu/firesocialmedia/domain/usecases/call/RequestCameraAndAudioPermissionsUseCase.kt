package com.minhtu.firesocialmedia.domain.usecases.call

import com.minhtu.firesocialmedia.domain.service.permission.PermissionManager

class RequestCameraAndAudioPermissionsUseCase(
    private val permissionManager: PermissionManager) {
        suspend operator fun invoke() : Boolean {
            return permissionManager.requestCameraAndAudioPermissions()
        }
    }