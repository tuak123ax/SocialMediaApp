package com.minhtu.firesocialmedia.data.remote.service.database

import cocoapods.FirebaseDatabase.FIRDataEventType
import cocoapods.FirebaseDatabase.FIRDataSnapshot
import cocoapods.FirebaseDatabase.FIRDatabase
import com.minhtu.firesocialmedia.constants.home.DataConstant
import com.minhtu.firesocialmedia.home.data.remote.dto.comment.CommentDTO
import com.minhtu.firesocialmedia.ios.service.serviceimpl.database.home.IosDatabaseHelper
import com.minhtu.firesocialmedia.ios.service.serviceimpl.database.supabase.SupabaseStorageHelper
import kotlinx.coroutines.CancellableContinuation
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.suspendCancellableCoroutine

private fun <T> CancellableContinuation<T>.resume(value: T) {
    this.resume(value, onCancellation = null)
}

private fun Map<String, Any?>.toCommentDTO(): CommentDTO {
    return CommentDTO(
        id = this["id"] as? String ?: "",
        posterId = this["posterId"] as? String ?: "",
        posterName = this["posterName"] as? String ?: "",
        avatar = this["avatar"] as? String ?: "",
        message = this["message"] as? String ?: "",
        image = this["image"] as? String ?: "",
        video = this["video"] as? String ?: "",
        likeCount = (this@toCommentDTO["likeCount"] as? Long)?.toInt() ?: 0,
        commentCount = (this@toCommentDTO["commentCount"] as? Long)?.toInt() ?: 0,
        timePosted = this@toCommentDTO["timePosted"] as? Long ?: 0L,
        listReplies = HashMap(
            (this["listReplies"] as? Map<*, *>)?.mapNotNull { (key, value) ->
                val k = key as? String
                val v = (value as? Map<String, Any?>)?.toCommentDTO()
                if (k != null && v != null) k to v else null
            }?.toMap() ?: emptyMap()
        )
    )
}

class IosHomeCommentDatabaseService : HomeCommentDatabaseService {
    override suspend fun getAllComments(
        path: String,
        newsId: String
    ): List<CommentDTO>? {
        val rawList = suspendCancellableCoroutine<List<CommentDTO>?> { continuation ->
            val result = mutableListOf<CommentDTO>()
            val databaseReference = FIRDatabase
                .database()
                .reference()
                .child(DataConstant.NEWS_PATH)
                .child(newsId)
                .child(path)

            databaseReference.observeSingleEventOfType(
                FIRDataEventType.FIRDataEventTypeValue,
                withBlock = { snapshot ->
                    if (snapshot != null && snapshot.exists()) {
                        result.clear()
                        val children = snapshot.children
                        while (true) {
                            val child = children.nextObject() as? FIRDataSnapshot ?: break
                            val rawValue = child.value as? Map<*, *> ?: continue

                            // Safely cast Map<*, *> to Map<String, Any?>
                            val value = rawValue.entries.associate {
                                (it.key as? String) to it.value
                            }.filterKeys { it != null } as Map<String, Any?>

                            try {
                                val comment = value.toCommentDTO()
                                result.add(comment)
                            } catch (e: Exception) {
                                e.printStackTrace()
                            }
                        }
                        if (continuation.isActive) continuation.resume(ArrayList(result))
                    } else {
                        if (continuation.isActive) continuation.resume(null)
                    }
                }
            )
        } ?: return null

        return coroutineScope {
            rawList.map { comment ->
                async {
                    if (comment.avatar.isNotEmpty()) comment.avatar = SupabaseStorageHelper.resolveMediaUrlAsync(comment.avatar)
                    if (comment.image.isNotEmpty()) comment.updateImage(SupabaseStorageHelper.resolveMediaUrlAsync(comment.image))
                    if (comment.video.isNotEmpty()) comment.updateVideo(SupabaseStorageHelper.resolveMediaUrlAsync(comment.video))
                    comment
                }
            }.awaitAll()
        }
    }

    override suspend fun updateCountValueInDatabase(
        id: String,
        path: String,
        externalPath: String,
        value: Int
    ) {
        IosDatabaseHelper.updateCountValueInDatabase(id, path, externalPath, value)
    }

    override suspend fun saveValueToDatabase(
        id: String,
        path: String,
        value: HashMap<String, Int>,
        externalPath: String
    ): Boolean {
        return IosDatabaseHelper.saveValueToDatabase(id, path, value, externalPath)
    }
}
