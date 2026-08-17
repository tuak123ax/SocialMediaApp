package com.minhtu.firesocialmedia.data.remote.service.database

import android.util.Log
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage
import com.minhtu.firesocialmedia.android.service.serviceimpl.database.supabase.SupabaseStorageHelper.Companion.resolveMediaUrlAsync
import com.minhtu.firesocialmedia.profile.data.remote.dto.news.NewsDTO
import kotlinx.coroutines.tasks.await
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

object AndroidProfileDatabaseHelper {
    suspend fun getNew(newId: String, newsPath: String): NewsDTO? {
        val raw = suspendCoroutine<NewsDTO?> { continuation ->
            val databaseReference = FirebaseDatabase.getInstance()
                .reference
                .child(newsPath)
                .child(newId)
            databaseReference.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    continuation.resume(snapshot.getValue(NewsDTO::class.java))
                }
                override fun onCancelled(error: DatabaseError) {
                    continuation.resume(null)
                }
            })
        } ?: return null

        raw.avatar = resolveMediaUrlAsync(raw.avatar)
        raw.image = resolveMediaUrlAsync(raw.image)
        raw.video = resolveMediaUrlAsync(raw.video)
        return raw
    }

    fun updateLikeCountForNew(newsId: String, value: Int, newsPath: String, likedCountPath: String) {
        if (value < 0) return
        FirebaseDatabase.getInstance()
            .reference
            .child(newsPath)
            .child(newsId)
            .child(likedCountPath)
            .setValue(value)
    }

    suspend fun deleteNewsFromDatabase(new: NewsDTO, newsPath: String): Boolean {
        return runCatching {
            FirebaseDatabase.getInstance()
                .reference
                .child(newsPath)
                .child(new.id)
                .removeValue()
                .await()

            if (new.image.isNotEmpty() || new.video.isNotEmpty()) {
                try {
                    FirebaseStorage.getInstance()
                        .reference
                        .child(newsPath)
                        .child(new.id)
                        .delete()
                        .await()
                } catch (e: Exception) {
                    Log.w("Task", "Storage delete: ${e.message}")
                }
            }
            true
        }.getOrElse {
            false
        }
    }
}
