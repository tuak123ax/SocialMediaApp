package com.minhtu.firesocialmedia.data.remote.service.database

import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.minhtu.firesocialmedia.android.service.serviceimpl.database.supabase.SupabaseStorageHelper.Companion.resolveMediaUrlAsync
import com.minhtu.firesocialmedia.constants.notification.DataConstant
import com.minhtu.firesocialmedia.notification.data.remote.dto.news.NewsDTO
import com.minhtu.firesocialmedia.notification.data.remote.dto.user.UserDTO
import kotlinx.coroutines.withTimeout
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

class AndroidNotificationDatabaseService : NotificationDatabaseService {
    override suspend fun getNew(newId: String, newsPath: String): NewsDTO? {
        return AndroidNotificationDatabaseHelper.getNew(newId, newsPath)
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
}
