package com.minhtu.firesocialmedia.data.remote.service.database

import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.minhtu.firesocialmedia.android.service.serviceimpl.database.supabase.SupabaseStorageHelper.Companion.resolveMediaUrlAsync
import com.minhtu.firesocialmedia.appinit.data.remote.dto.user.UserDTO
import com.minhtu.firesocialmedia.constants.search.DataConstant
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.withTimeout
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

/**
 * Android implementation of appInit's own [AppInitDatabaseService], moved out of core's
 * `AndroidDatabaseService` so that core no longer needs to know about the User-typed reads the
 * search screen needs. Mirrors feature/home's `AndroidHomeDatabaseService.getUser`/
 * `searchUserByName` exactly.
 */
class AndroidAppInitDatabaseService : AppInitDatabaseService {
    override suspend fun getUser(userId: String): UserDTO? {
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

        raw.image = resolveMediaUrlAsync(raw.image)
        return raw
    }

    override suspend fun searchUserByName(
        name: String,
        path: String
    ): List<UserDTO>? {
        val raw = withTimeout(5000) {
            val database = FirebaseDatabase.getInstance()
            val databaseReference = database.getReference(path)
            suspendCoroutine<List<UserDTO>?> { continuation ->
                databaseReference.addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        val users = snapshot.children
                            .mapNotNull { it.getValue(UserDTO::class.java) }
                            .filter { it.name.contains(name, ignoreCase = true) }
                            .take(5)
                        continuation.resume(users)
                    }
                    override fun onCancelled(error: DatabaseError) {
                        continuation.resume(null)
                    }
                })
            }
        } ?: return null

        return coroutineScope {
            raw.map { user ->
                async { user.copy(image = resolveMediaUrlAsync(user.image)) }
            }.awaitAll()
        }
    }
}
