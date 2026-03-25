package com.minhtu.firesocialmedia.domain.serviceimpl.permission

import android.Manifest
import android.app.Activity
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.minhtu.firesocialmedia.data.remote.service.permission.PermissionManager
import kotlinx.coroutines.CancellableContinuation
import kotlinx.coroutines.suspendCancellableCoroutine
import java.lang.ref.WeakReference

class AndroidPermissionManager(activity: Activity?) : PermissionManager {
    private var continuation: CancellableContinuation<Boolean>? = null
    private var activityRef: WeakReference<Activity>? = activity?.let { WeakReference(it) }

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
                cont.resume(false, onCancellation = {})
                return@suspendCancellableCoroutine
            }
            // If already granted, return immediately
            val allGranted = permissions.all {
                ContextCompat.checkSelfPermission(currentActivity, it) == PackageManager.PERMISSION_GRANTED
            }
            if (allGranted) {
                cont.resume(true, onCancellation = {})
                continuation = null
                return@suspendCancellableCoroutine
            }
            ActivityCompat.requestPermissions(currentActivity, permissions, REQUEST_CODE)
        }
    }

    fun onRequestPermissionsResult(requestCode: Int, grantResults: IntArray) {
        if (requestCode != REQUEST_CODE) return
        val granted = grantResults.all { it == PackageManager.PERMISSION_GRANTED }
        continuation?.resume(granted, onCancellation = {})
        continuation = null
    }

    fun clear() {
        continuation = null
        activityRef?.clear()
        activityRef = null
    }

    companion object {
        private const val REQUEST_CODE = 100
    }
}