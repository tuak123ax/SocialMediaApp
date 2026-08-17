package com.minhtu.firesocialmedia.android.service.serviceimpl.database.profile

import android.util.Log
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout
import kotlin.coroutines.resume

class AndroidDatabaseHelper {
    companion object {
        suspend fun saveValueToDatabase(
            id: String,
            path: String,
            value: HashMap<String, Int>,
            externalPath: String
        ): Boolean = suspendCancellableCoroutine { continuation ->
            Log.d("Task", "saveValueToDatabase")
            var databaseReference = FirebaseDatabase.getInstance().getReference()
                .child(path).child(id)
            if (externalPath.isNotEmpty()) {
                databaseReference = databaseReference.child(externalPath)
            }
            if (value.isNotEmpty()) {
                databaseReference.setValue(value).addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        if (continuation.isActive) continuation.resume(true)
                    } else {
                        if (continuation.isActive) continuation.resume(false)
                    }
                }
            } else {
                databaseReference.removeValue().addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        if (continuation.isActive) continuation.resume(true)
                    } else {
                        if (continuation.isActive) continuation.resume(false)
                    }
                }
            }
            Log.d("Task", "Finish saving Value To Database")
        }

        suspend fun saveListToDatabase(
            id: String,
            path: String,
            value: ArrayList<String>,
            externalPath: String
        ) {
            Log.d("Task", "saveListToDatabase")
            var databaseReference = FirebaseDatabase.getInstance().getReference()
                .child(path).child(id)
            if (externalPath.isNotEmpty()) {
                databaseReference = databaseReference.child(externalPath)
            }
            withContext(Dispatchers.Main) {
                databaseReference.setValue(value)
            }
            Log.d("Task", "Finish saving List To Database")
        }

        suspend fun updateUserStringField(
            userId: String,
            fieldPath: String,
            value: String,
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
                    val shouldRetry = e is kotlinx.io.IOException

                    if (attempt < 2 && shouldRetry) {
                        kotlinx.coroutines.delay(delayTime)
                        delayTime *= 2
                    } else {
                        Log.e("Firebase", "Failed to update user string field", e)
                        return false
                    }
                }
            }

            return false
        }

        suspend fun updateUserAvatar(
            userId: String,
            imageUri: String,
            userPath: String
        ): Boolean {
            return try {
                val helper = com.minhtu.firesocialmedia.android.service.serviceimpl.database.supabase.SupabaseStorageHelper()
                val extension = helper.getFileExtension(imageUri, "jpg")
                val remotePath = "avatar/${userId}_${System.currentTimeMillis()}.$extension"
                com.minhtu.firesocialmedia.android.service.serviceimpl.database.supabase.profile.SupabaseStorage.upload(
                    filePath = imageUri,
                    remotePath = remotePath
                )
                updateUserStringField(userId, "image", remotePath, userPath)
            } catch (e: Exception) {
                Log.e("Firebase", "Failed to update user avatar", e)
                false
            }
        }

        suspend fun updateUserBackground(
            userId: String,
            imageUri: String,
            userPath: String
        ): Boolean {
            return try {
                val helper = com.minhtu.firesocialmedia.android.service.serviceimpl.database.supabase.SupabaseStorageHelper()
                val extension = helper.getFileExtension(imageUri, "jpg")
                val remotePath = "background/${userId}_${System.currentTimeMillis()}.$extension"
                com.minhtu.firesocialmedia.android.service.serviceimpl.database.supabase.profile.SupabaseStorage.upload(
                    filePath = imageUri,
                    remotePath = remotePath
                )
                updateUserStringField(userId, "background", remotePath, userPath)
            } catch (e: Exception) {
                Log.e("Firebase", "Failed to update user background", e)
                false
            }
        }
    }
}
