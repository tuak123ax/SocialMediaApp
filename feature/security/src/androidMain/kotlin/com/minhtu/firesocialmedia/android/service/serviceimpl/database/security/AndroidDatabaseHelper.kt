package com.minhtu.firesocialmedia.android.service.serviceimpl.database.security

import android.app.DownloadManager
import android.content.Context
import android.os.Environment
import android.util.Log
import androidx.core.net.toUri
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.delay
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withTimeout
import kotlinx.io.IOException
import kotlin.coroutines.resume

class AndroidDatabaseHelper {
    companion object {
        suspend fun downloadImage(context: Context, image: String, fileName: String): Boolean =
            suspendCancellableCoroutine { continuation ->
                val request = DownloadManager.Request(image.toUri())
                    .setTitle("Download Image")
                    .setDescription("Downloading $fileName")
                    .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
                    .setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, fileName)
                    .setAllowedOverMetered(true)
                    .setAllowedOverRoaming(true)

                val downloadManager =
                    context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
                val result = downloadManager.enqueue(request)
                if (result == -1L) {
                    if (continuation.isActive) continuation.resume(false)
                } else {
                    if (continuation.isActive) continuation.resume(true)
                }
            }

        suspend fun updateTwoFAEnabledFlagForUser(
            userId: String,
            twoFAEnabled: Boolean,
            userPath: String,
            twoFaEnabledPath: String
        ): Boolean {
            val ref = FirebaseDatabase.getInstance()
                .reference
                .child(userPath)
                .child(userId)
                .child(twoFaEnabledPath)

            var delayTime = 200L

            repeat(3) { attempt ->
                try {
                    withTimeout(3000) {
                        ref.setValue(twoFAEnabled).await()
                    }
                    return true
                } catch (e: Exception) {
                    val shouldRetry = e is IOException

                    if (attempt < 2 && shouldRetry) {
                        delay(delayTime)
                        delayTime *= 2
                    } else {
                        Log.e("Firebase", "Failed to update 2FA flag", e)
                        return false
                    }
                }
            }

            return false
        }

        suspend fun updateUserLongField(
            userId: String,
            fieldPath: String,
            value: Long,
            userPath: String
        ): Boolean {
            val ref = FirebaseDatabase.getInstance()
                .reference
                .child(userPath)
                .child(userId)
                .child(fieldPath)

            var delayTime = 200L

            repeat(3) { attempt ->
                try {
                    withTimeout(3000) {
                        ref.setValue(value).await()
                    }
                    return true
                } catch (e: Exception) {
                    val shouldRetry = e is IOException

                    if (attempt < 2 && shouldRetry) {
                        delay(delayTime)
                        delayTime *= 2
                    } else {
                        Log.e("Firebase", "Failed to update user long field", e)
                        return false
                    }
                }
            }

            return false
        }
    }
}
