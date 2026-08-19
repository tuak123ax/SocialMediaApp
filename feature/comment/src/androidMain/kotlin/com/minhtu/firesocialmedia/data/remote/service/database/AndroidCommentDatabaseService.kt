package com.minhtu.firesocialmedia.data.remote.service.database

import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.minhtu.firesocialmedia.constants.comment.DataConstant
import com.minhtu.firesocialmedia.data.remote.dto.comment.CommentDTO
import com.minhtu.firesocialmedia.android.service.serviceimpl.database.comment.AndroidDatabaseHelper
import com.minhtu.firesocialmedia.android.service.serviceimpl.database.supabase.SupabaseStorageHelper.Companion.resolveMediaUrlAsync
import com.minhtu.firesocialmedia.comment.data.remote.dto.user.UserDTO
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withTimeout
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

class AndroidCommentDatabaseService : CommentDatabaseService {
    override suspend fun getAllComments(
        path: String,
        newsId: String
    ): List<CommentDTO>? {
        // Phase 1: fetch raw comments from Firebase
        val raw = suspendCancellableCoroutine<List<CommentDTO>?> { continuation ->
            val databaseReference: DatabaseReference = FirebaseDatabase.getInstance()
                .getReference()
                .child(DataConstant.NEWS_PATH)
                .child(newsId)
                .child(path)
            val listener = object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (!continuation.isActive) return
                    val comments = snapshot.children.mapNotNull { it.getValue(CommentDTO::class.java) }
                    databaseReference.removeEventListener(this)
                    continuation.resume(comments)
                }
                override fun onCancelled(error: DatabaseError) {
                    if (!continuation.isActive) return
                    databaseReference.removeEventListener(this)
                    continuation.resume(null)
                }
            }
            databaseReference.addValueEventListener(listener)
            continuation.invokeOnCancellation { databaseReference.removeEventListener(listener) }
        } ?: return null

        // Phase 2: async resolve media URLs in parallel
        return coroutineScope {
            raw.map { comment ->
                async {
                    comment.copy(
                        avatar = resolveMediaUrlAsync(comment.avatar),
                        image = resolveMediaUrlAsync(comment.image),
                        video = resolveMediaUrlAsync(comment.video)
                    )
                }
            }.awaitAll()
        }
    }

    override suspend fun getUser(userId: String): UserDTO? {
        // Phase 1: fetch raw user from Firebase
        val raw = withTimeout(5000) {
            suspendCoroutine<UserDTO?> { continuation ->
                val database = FirebaseDatabase.getInstance()
                val databaseReference = database.getReference()
                    .child(DataConstant.USER_PATH)
                    .child(userId)
                databaseReference.addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        continuation.resume(snapshot.getValue(UserDTO::class.java))
                    }
                    override fun onCancelled(error: DatabaseError) {
                        continuation.resume(null)
                    }
                })
            }
        } ?: return null

        // Phase 2: resolve user image URL
        raw.image = resolveMediaUrlAsync(raw.image)
        return raw
    }

    override suspend fun updateCountValueInDatabase(
        id: String,
        path: String,
        externalPath: String,
        value: Int
    ) {
        AndroidDatabaseHelper.updateCountValueInDatabase(id, path, externalPath, value)
    }

    override suspend fun saveValueToDatabase(
        id: String,
        path: String,
        value: HashMap<String, Int>,
        externalPath: String
    ): Boolean {
        return AndroidDatabaseHelper.saveValueToDatabase(id, path, value, externalPath)
    }
}
