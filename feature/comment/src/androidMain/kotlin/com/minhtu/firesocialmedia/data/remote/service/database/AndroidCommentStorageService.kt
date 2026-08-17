package com.minhtu.firesocialmedia.data.remote.service.database

import android.util.Log
import com.google.firebase.database.FirebaseDatabase
import com.minhtu.firesocialmedia.android.service.serviceimpl.database.supabase.comment.SupabaseStorage
import com.minhtu.firesocialmedia.android.service.serviceimpl.database.supabase.SupabaseStorageHelper
import com.minhtu.firesocialmedia.domain.entity.base.BaseNewsInstance
import com.minhtu.firesocialmedia.platform.logMessage
import kotlinx.coroutines.tasks.await

class AndroidCommentStorageService : CommentStorageService {
    private val fileExtensionHelper = SupabaseStorageHelper()

    override suspend fun saveInstanceToDatabase(
        id: String,
        path: String,
        instance: BaseNewsInstance
    ): Boolean {
        val databaseReference = FirebaseDatabase.getInstance()
            .getReference()
            .child(path)
            .child(id)

        return try {
            when {
                instance.image.isNotEmpty() -> {
                    val extension = fileExtensionHelper.getFileExtension(instance.image, "jpg")
                    val remotePath = "$path/${id}_${System.currentTimeMillis()}.$extension"

                    SupabaseStorage.upload(
                        filePath = instance.image,
                        remotePath = remotePath
                    )

                    instance.updateImage(remotePath)
                    instance.updateVideo("")
                }

                instance.video.isNotEmpty() -> {
                    val extension = fileExtensionHelper.getFileExtension(instance.video, "mp4")
                    val remotePath = "$path/${id}_${System.currentTimeMillis()}.$extension"

                    SupabaseStorage.upload(
                        filePath = instance.video,
                        remotePath = remotePath
                    )

                    instance.updateVideo(remotePath)
                    instance.updateImage("")
                }

                else -> {
                    // no media
                }
            }

            databaseReference.setValue(instance).await()
            true
        } catch (e: Exception) {
            logMessage(
                "AndroidCommentStorageService",
                { "saveInstanceToDatabase failed: ${e.message}" }
            )
            false
        }
    }

    override suspend fun deleteCommentFromDatabase(path: String, comment: BaseNewsInstance) {
        Log.d("Task", "deleteCommentFromDatabase")
        FirebaseDatabase.getInstance().getReference()
            .child(path).child(comment.id).removeValue().await()
    }
}
