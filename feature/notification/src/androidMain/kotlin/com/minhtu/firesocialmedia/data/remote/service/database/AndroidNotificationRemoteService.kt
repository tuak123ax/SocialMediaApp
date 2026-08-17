package com.minhtu.firesocialmedia.data.remote.service.database

import android.util.Log
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.minhtu.firesocialmedia.android.service.serviceimpl.database.supabase.SupabaseStorageHelper.Companion.resolveMediaUrlAsync
import com.minhtu.firesocialmedia.constants.notification.DataConstant
import com.minhtu.firesocialmedia.notification.data.remote.dto.notification.NotificationDTO
import com.minhtu.firesocialmedia.platform.logMessage
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.tasks.await

class AndroidNotificationRemoteService : NotificationRemoteService {
    override suspend fun getAllNotificationsOfUser(
        path: String,
        currentUserUid: String
    ): List<NotificationDTO>? {
        // Phase 1: fetch raw notifications from Firebase
        val raw = suspendCancellableCoroutine<List<NotificationDTO>?> { continuation ->
            val databaseReference = FirebaseDatabase.getInstance()
                .getReference(DataConstant.USER_PATH)
                .child(currentUserUid)
                .child(path)
            val listener = object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val result = snapshot.children
                        .mapNotNull { it.getValue(NotificationDTO::class.java) }
                    if (continuation.isActive) {
                        databaseReference.removeEventListener(this)
                        continuation.resume(result) { }
                    }
                }
                override fun onCancelled(error: DatabaseError) {
                    if (continuation.isActive) {
                        databaseReference.removeEventListener(this)
                        continuation.resume(null) { }
                    }
                }
            }
            databaseReference.addValueEventListener(listener)
            continuation.invokeOnCancellation { databaseReference.removeEventListener(listener) }
        } ?: return null

        // Phase 2: async resolve avatar URLs in parallel
        return coroutineScope {
            raw.map { notification ->
                async {
                    val resolved = notification.copy(avatar = resolveMediaUrlAsync(notification.avatar))
                    logMessage("getAllNotificationsOfUser") { "${resolved.id} isRead: ${resolved.beRead}" }
                    resolved
                }
            }.awaitAll()
        }
    }

    override suspend fun saveNotificationToDatabase(
        id: String,
        path: String,
        instance: ArrayList<NotificationDTO>
    ) {
        Log.d("Task", "saveNotificationToDatabase")
        val databaseReference = FirebaseDatabase.getInstance().getReference()
            .child(path).child(id).child(DataConstant.NOTIFICATION_PATH)
        databaseReference.setValue(instance)
    }

    override suspend fun deleteNotificationFromDatabase(
        id: String,
        path: String,
        notification: NotificationDTO
    ) {
        Log.d("Task", "deleteNotificationFromDatabase")
        val databaseReference = FirebaseDatabase.getInstance().getReference()
            .child(path).child(id).child(DataConstant.NOTIFICATION_PATH)
        databaseReference.get().addOnSuccessListener { snapshot ->
            //Get notification list from db - iterate children to avoid GenericTypeIndicator
            //which breaks under R8/ProGuard obfuscation in release builds
            val list = snapshot.children
                .mapNotNull { it.getValue(NotificationDTO::class.java) }
                .toMutableList()
            //Delete value by id to avoid equality issues with resolved URLs or mutated fields
            list.removeIf { it.id == notification.id }
            if (list.isEmpty()) {
                databaseReference.removeValue() // clean up the node entirely
            } else {
                databaseReference.setValue(list) // overwrite with updated list
            }
        }
    }

    override suspend fun updateIsReadStatusOfNotification(
        userId: String,
        notificationId: String,
        userPath: String,
        notificationPath: String
    ) {
        val notificationsRef = FirebaseDatabase
            .getInstance()
            .reference
            .child(userPath)
            .child(userId)
            .child(notificationPath)

        notificationsRef.get().addOnSuccessListener { snapshot ->
            snapshot.children.forEach { child ->
                val id = child.child("id").getValue(String::class.java)
                if (id == notificationId) {
                    child.ref.child("beRead").setValue(true)
                    return@addOnSuccessListener
                }
            }
        }
    }

    override suspend fun deleteAllNotifications(
        uid: String,
        userPath: String,
        notificationPath: String
    ): Result<Unit> {
        return try {
            FirebaseDatabase.getInstance()
                .reference
                .child(userPath)
                .child(uid)
                .child(notificationPath)
                .removeValue()
                .await()

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
