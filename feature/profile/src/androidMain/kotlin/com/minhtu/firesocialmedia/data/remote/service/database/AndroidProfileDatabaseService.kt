package com.minhtu.firesocialmedia.data.remote.service.database

import android.util.Log
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.minhtu.firesocialmedia.android.service.serviceimpl.database.profile.AndroidDatabaseHelper
import com.minhtu.firesocialmedia.android.service.serviceimpl.database.supabase.SupabaseStorageHelper.Companion.resolveMediaUrlAsync
import com.minhtu.firesocialmedia.constants.profile.DataConstant
import com.minhtu.firesocialmedia.profile.data.remote.dto.news.NewsDTO
import com.minhtu.firesocialmedia.profile.data.remote.dto.news.ProfileLatestNewsDTO
import com.minhtu.firesocialmedia.profile.data.remote.dto.user.UserDTO
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withTimeout
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

class AndroidProfileDatabaseService : ProfileDatabaseService {
    override suspend fun getNew(newId: String, newsPath: String): NewsDTO? {
        return AndroidProfileDatabaseHelper.getNew(newId, newsPath)
    }

    override suspend fun getNewsByPoster(
        posterId: String,
        number: Int,
        lastTimePosted: Double?,
        lastKey: String?,
        newsPath: String
    ): ProfileLatestNewsDTO {
        return AndroidProfileDatabaseHelper.getNewsByPoster(posterId, number, lastTimePosted, lastKey, newsPath)
    }

    override suspend fun deleteNewsFromDatabase(new: NewsDTO, newsPath: String): Boolean {
        return AndroidProfileDatabaseHelper.deleteNewsFromDatabase(new, newsPath)
    }

    override suspend fun updateLikeCountForNew(newsId: String, value: Int, newsPath: String, likedCountPath: String) {
        AndroidProfileDatabaseHelper.updateLikeCountForNew(newsId, value, newsPath, likedCountPath)
    }

    override suspend fun deletePollFromDatabase(
        newsId: String,
        pollId: String,
        groupPath: String,
        groupId: String,
        postsPath: String,
        pollPath: String,
        pollVotesPath: String
    ): Boolean = suspendCancellableCoroutine { continuation ->
        Log.d("Task", "deletePollFromDatabase: newsId=$newsId pollId=$pollId groupId=$groupId")
        val databaseRef = FirebaseDatabase.getInstance().reference
        val updates = hashMapOf<String, Any?>(
            "$groupPath/$groupId/$postsPath/$newsId" to null,
            "$pollPath/$pollId" to null,
            "$pollVotesPath/$pollId" to null
        )
        databaseRef.updateChildren(updates).addOnCompleteListener { task ->
            if (!continuation.isActive) return@addOnCompleteListener
            if (task.isSuccessful) {
                Log.d("Task", "deletePollFromDatabase success")
            } else {
                Log.e("Task", "deletePollFromDatabase FAILED", task.exception)
            }
            continuation.resume(task.isSuccessful)
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

    override suspend fun saveValueToDatabase(id: String, path: String, value: HashMap<String, Int>, externalPath: String): Boolean {
        return AndroidDatabaseHelper.saveValueToDatabase(id, path, value, externalPath)
    }

    override suspend fun saveListToDatabase(id: String, path: String, value: ArrayList<String>, externalPath: String) {
        AndroidDatabaseHelper.saveListToDatabase(id, path, value, externalPath)
    }

    override suspend fun updateUserStringField(userId: String, fieldPath: String, value: String, userPath: String): Boolean {
        return AndroidDatabaseHelper.updateUserStringField(userId, fieldPath, value, userPath)
    }

    override suspend fun updateUserAvatar(userId: String, imageUri: String, userPath: String): Boolean {
        return AndroidDatabaseHelper.updateUserAvatar(userId, imageUri, userPath)
    }

    override suspend fun updateUserBackground(userId: String, imageUri: String, userPath: String): Boolean {
        return AndroidDatabaseHelper.updateUserBackground(userId, imageUri, userPath)
    }

    override suspend fun anyChildMatchesFieldValue(path: String, fields: List<String>, value: String): Boolean? =
        suspendCancellableCoroutine { continuation ->
            val ref = FirebaseDatabase.getInstance().getReference(path)
            ref.get().addOnSuccessListener { snapshot ->
                var matched = false
                for (child in snapshot.children) {
                    for (field in fields) {
                        if (child.child(field).getValue(String::class.java) == value) {
                            matched = true
                            break
                        }
                    }
                    if (matched) break
                }
                if (continuation.isActive) continuation.resume(matched)
            }.addOnFailureListener {
                if (continuation.isActive) continuation.resume(false)
            }
        }
}
