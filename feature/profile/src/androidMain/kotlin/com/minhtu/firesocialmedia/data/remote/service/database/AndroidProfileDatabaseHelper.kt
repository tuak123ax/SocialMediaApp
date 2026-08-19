package com.minhtu.firesocialmedia.data.remote.service.database

import android.util.Log
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage
import com.minhtu.firesocialmedia.android.service.serviceimpl.database.supabase.SupabaseStorageHelper.Companion.resolveMediaUrlAsync
import com.minhtu.firesocialmedia.platform.logMessage
import com.minhtu.firesocialmedia.profile.data.remote.dto.news.NewsDTO
import com.minhtu.firesocialmedia.profile.data.remote.dto.news.ProfileLatestNewsDTO
import kotlinx.coroutines.tasks.await
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

object AndroidProfileDatabaseHelper {
    // How many raw feed entries (ordered by timePosted, unfiltered) to pull per round trip while
    // hunting for posts by a specific poster. There's no composite (posterId, timePosted) index,
    // so we can't ask Firebase for "the next N posts by this user" directly.
    private const val RAW_PAGE_SIZE = 20


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

    suspend fun getNewsByPoster(
        posterId: String,
        number: Int,
        lastTimePosted: Double?,
        lastKey: String?,
        newsPath: String
    ): ProfileLatestNewsDTO {
        val matched = mutableListOf<NewsDTO>()
        var cursorTime = lastTimePosted
        var cursorKey = lastKey
        var exhausted = false

        while (matched.size < number && !exhausted) {
            val page = suspendCoroutine { continuation ->
                FirebaseDatabase.getInstance()
                    .reference
                    .child(newsPath)
                    .orderByChild("timePosted")
                    .let { query ->
                        when {
                            cursorTime != null && !cursorKey.isNullOrBlank() -> query.endBefore(cursorTime, cursorKey)
                            cursorTime != null -> query.endBefore(cursorTime)
                            else -> query
                        }
                    }
                    .limitToLast(RAW_PAGE_SIZE)
                    .addListenerForSingleValueEvent(object : ValueEventListener {
                        override fun onDataChange(snapshot: DataSnapshot) {
                            val raw = snapshot.children.mapNotNull { it.getValue(NewsDTO::class.java) }
                            if (raw.isEmpty()) {
                                continuation.resume(Triple(emptyList(), null, null))
                                return
                            }
                            val sorted = raw.sortedByDescending { it.timePosted }
                            val oldest = sorted.last()
                            continuation.resume(Triple(sorted, oldest.timePosted.toDouble(), oldest.id))
                        }
                        override fun onCancelled(error: DatabaseError) {
                            continuation.resume(Triple(emptyList(), null, null))
                        }
                    })
            }

            val (rawNews, nextTime, nextKey) = page
            if (rawNews.isEmpty()) {
                exhausted = true
                break
            }
            matched += rawNews.filter { it.posterId == posterId }
            cursorTime = nextTime
            cursorKey = nextKey
            if (rawNews.size < RAW_PAGE_SIZE) {
                exhausted = true
            }
        }

        val resolved = matched.map { news ->
            news.avatar = resolveMediaUrlAsync(news.avatar)
            news.image = resolveMediaUrlAsync(news.image)
            news.video = resolveMediaUrlAsync(news.video)
            news
        }

        return ProfileLatestNewsDTO(
            news = resolved,
            lastTimePostedValue = if (exhausted) null else cursorTime,
            lastKeyValue = if (exhausted) null else cursorKey
        )
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
