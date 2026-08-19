package com.minhtu.firesocialmedia.data.remote.service.database

import cocoapods.FirebaseDatabase.FIRDatabase
import cocoapods.FirebaseDatabase.FIRDatabaseReference
import com.minhtu.firesocialmedia.domain.entity.base.BaseNewsInstance
import com.minhtu.firesocialmedia.ios.service.serviceimpl.database.supabase.comment.SupabaseStorage
import com.minhtu.firesocialmedia.platform.getCurrentTime
import com.minhtu.firesocialmedia.platform.logMessage
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi

class IosCommentStorageService : CommentStorageService {
    @OptIn(ExperimentalEncodingApi::class)
    override suspend fun saveInstanceToDatabase(
        id: String,
        path: String,
        instance: BaseNewsInstance
    ): Boolean {
        val dbRef = FIRDatabase.database().reference().child(path).child(id)

        return try {
            when {
                instance.image.isNotEmpty() -> {
                    val bytes = Base64.Default.decode(instance.image)
                    val remotePath = "$path/${id}_${getCurrentTime()}.jpg"
                    SupabaseStorage.uploadBytes(bytes, remotePath)
                    instance.updateImage(remotePath)
                    instance.updateVideo("")
                }

                instance.video.isNotEmpty() -> {
                    val remotePath = "$path/${id}_${getCurrentTime()}.mp4"
                    SupabaseStorage.uploadFile(instance.video, remotePath)
                    instance.updateVideo(remotePath)
                    instance.updateImage("")
                }

                else -> {
                    // no media
                }
            }

            setValueSuspend(dbRef, instance.toMap())
            true
        } catch (e: Exception) {
            logMessage("IosCommentStorageService", { "saveInstanceToDatabase failed: ${e.message}" })
            false
        }
    }

    override suspend fun deleteCommentFromDatabase(path: String, comment: BaseNewsInstance) {
        try {
            val dbRef = FIRDatabase.database().reference().child(path).child(comment.id)
            removeValueSuspend(dbRef)
        } catch (e: Exception) {
            logMessage("IosCommentStorageService", { "deleteCommentFromDatabase failed: ${e.message}" })
        }
    }

    private suspend fun setValueSuspend(ref: FIRDatabaseReference, value: Map<String, Any?>) =
        suspendCancellableCoroutine<Unit> { cont ->
            ref.setValue(value) { error, _ ->
                if (error == null) cont.resume(Unit)
                else cont.resumeWithException(Throwable(error.localizedDescription))
            }
        }

    private suspend fun removeValueSuspend(ref: FIRDatabaseReference) =
        suspendCancellableCoroutine<Unit> { cont ->
            ref.removeValueWithCompletionBlock { error, _ ->
                if (error == null) cont.resume(Unit)
                else cont.resumeWithException(Throwable(error.localizedDescription))
            }
        }
}
