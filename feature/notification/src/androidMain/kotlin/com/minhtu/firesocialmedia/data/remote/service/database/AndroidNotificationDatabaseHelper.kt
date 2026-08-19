package com.minhtu.firesocialmedia.data.remote.service.database

import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.minhtu.firesocialmedia.android.service.serviceimpl.database.supabase.SupabaseStorageHelper.Companion.resolveMediaUrlAsync
import com.minhtu.firesocialmedia.notification.data.remote.dto.news.NewsDTO
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

object AndroidNotificationDatabaseHelper {
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
}
