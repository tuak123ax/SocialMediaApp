package com.minhtu.firesocialmedia.domain.permission

import android.Manifest
import android.app.Activity
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat
import com.minhtu.firesocialmedia.domain.PermissionManager
import kotlinx.coroutines.CancellableContinuation
import kotlinx.coroutines.suspendCancellableCoroutine

class AndroidPermissionManager(private val activity: Activity) : PermissionManager {
    private var continuation: CancellableContinuation<Boolean>? = null

    override suspend fun requestCameraAndAudioPermissions(): Boolean {
        return requestPermissions(
            arrayOf(
                Manifest.permission.CAMERA,
                Manifest.permission.RECORD_AUDIO
            )
        )
    }

    override suspend fun requestAudioPermission(): Boolean {
        return requestPermissions(arrayOf(
            Manifest.permission.RECORD_AUDIO))
    }

    private suspend fun requestPermissions(permissions: Array<String>): Boolean {
        return suspendCancellableCoroutine { cont ->
            continuation = cont
            ActivityCompat.requestPermissions(activity, permissions, REQUEST_CODE)
        }
    }

    fun onRequestPermissionsResult(requestCode: Int, grantResults: IntArray) {
        if (requestCode != REQUEST_CODE) return
        val granted = grantResults.all { it == PackageManager.PERMISSION_GRANTED }
        continuation?.resume(granted, onCancellation = {})
        continuation = null
    }

    companion object {
        private const val REQUEST_CODE = 100
    }
}