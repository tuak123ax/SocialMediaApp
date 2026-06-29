package com.minhtu.firesocialmedia.domain.serviceimpl.permission

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.result.ActivityResultLauncher
import androidx.core.content.ContextCompat
import androidx.core.app.ActivityCompat
import android.app.Activity
import com.minhtu.firesocialmedia.data.remote.service.permission.PermissionManager
import kotlinx.coroutines.CancellableContinuation
import kotlinx.coroutines.suspendCancellableCoroutine
import java.lang.ref.WeakReference

class AndroidPermissionManager(activity: Activity?) : PermissionManager {
    private var continuation: CancellableContinuation<Boolean>? = null
    private var activityRef: WeakReference<Activity>? = activity?.let { WeakReference(it) }
    private var permissionLauncher: ActivityResultLauncher<Array<String>>? = null

    fun setPermissionLauncher(launcher: ActivityResultLauncher<Array<String>>) {
        permissionLauncher = launcher
    }

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
            val currentActivity = activityRef?.get()
            if (currentActivity == null) {
                cont.resume(false, onCancellation = null)
                return@suspendCancellableCoroutine
            }
            // If already granted, return immediately
            val allGranted = permissions.all {
                ContextCompat.checkSelfPermission(currentActivity, it) == PackageManager.PERMISSION_GRANTED
            }
            if (allGranted) {
                cont.resume(true, onCancellation = null)
                continuation = null
                return@suspendCancellableCoroutine
            }
            val launcher = permissionLauncher
            if (launcher != null) {
                launcher.launch(permissions)
            } else {
                // Fallback to legacy API if launcher not set
                ActivityCompat.requestPermissions(currentActivity, permissions, REQUEST_CODE)
            }
        }
    }

    fun onPermissionsResult(grantResults: Map<String, Boolean>) {
        val granted = grantResults.values.all { it }
        continuation?.resume(granted, onCancellation = null)
        continuation = null
    }

    // Legacy callback kept for fallback compatibility
    fun onRequestPermissionsResult(requestCode: Int, grantResults: IntArray) {
        if (requestCode != REQUEST_CODE) return
        val granted = grantResults.all { it == PackageManager.PERMISSION_GRANTED }
        continuation?.resume(granted, onCancellation = null)
        continuation = null
    }

    fun clear() {
        continuation = null
        activityRef?.clear()
        activityRef = null
        permissionLauncher = null
    }

    companion object {
        private const val REQUEST_CODE = 100
    }
}
