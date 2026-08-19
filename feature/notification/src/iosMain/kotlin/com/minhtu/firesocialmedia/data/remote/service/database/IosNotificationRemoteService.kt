package com.minhtu.firesocialmedia.data.remote.service.database

import cocoapods.FirebaseDatabase.FIRDataEventType
import cocoapods.FirebaseDatabase.FIRDataSnapshot
import cocoapods.FirebaseDatabase.FIRDatabase
import cocoapods.FirebaseDatabase.FIRDatabaseReference
import com.minhtu.firesocialmedia.constants.notification.DataConstant
import com.minhtu.firesocialmedia.notification.data.remote.dto.notification.NotificationDTO
import com.minhtu.firesocialmedia.notification.data.remote.dto.notification.fromMap
import com.minhtu.firesocialmedia.notification.data.remote.dto.notification.toMap
import com.minhtu.firesocialmedia.ios.service.serviceimpl.database.supabase.SupabaseStorageHelper
import com.minhtu.firesocialmedia.platform.logMessage
import kotlinx.cinterop.BetaInteropApi
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.suspendCancellableCoroutine
import platform.Foundation.NSMutableArray
import platform.Foundation.NSMutableDictionary
import platform.Foundation.NSNumber
import platform.Foundation.NSString
import platform.Foundation.create
import platform.Foundation.numberWithBool
import platform.Foundation.numberWithDouble
import platform.Foundation.numberWithFloat
import platform.Foundation.numberWithInt
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

class IosNotificationRemoteService : NotificationRemoteService {
    private val database: FIRDatabaseReference = FIRDatabase.database().reference()

    override suspend fun getAllNotificationsOfUser(
        path: String,
        currentUserUid: String
    ): List<NotificationDTO>? {
        val rawList = suspendCancellableCoroutine<List<NotificationDTO>?> { continuation ->
            val result = mutableListOf<NotificationDTO>()
            val databaseReference = FIRDatabase.database().reference()
                .child(DataConstant.USER_PATH)
                .child(currentUserUid)
                .child(path)

            databaseReference.observeSingleEventOfType(
                FIRDataEventType.FIRDataEventTypeValue,
                withBlock = { snapshot ->
                    result.clear()
                    if (snapshot != null && snapshot.exists()) {
                        val children = snapshot.children
                        while (true) {
                            val child = children.nextObject() as? FIRDataSnapshot ?: break
                            val value = child.value as? Map<*, *> ?: continue

                            try {
                                val notification = NotificationDTO.fromMap(value as Map<String, Any>)
                                result.add(notification)
                            } catch (e: Exception) {
                                e.printStackTrace()
                            }
                        }
                        if (continuation.isActive) continuation.resume(result)
                    } else {
                        if (continuation.isActive) continuation.resume(null)
                    }
                }
            ) { error ->
                logMessage("getAllNotificationsOfUser", { "Error: ${error?.localizedDescription}" })
                if (continuation.isActive) continuation.resume(null)
            }
        } ?: return null

        // Phase 2: resolve notification avatars — NotificationDTO.avatar is val, use copy()
        return coroutineScope {
            rawList.map { notification ->
                async {
                    val resolvedAvatar = if (notification.avatar.isNotEmpty())
                        SupabaseStorageHelper.resolveMediaUrlAsync(notification.avatar)
                    else notification.avatar
                    notification.copy(avatar = resolvedAvatar)
                }
            }.awaitAll()
        }
    }

    override suspend fun saveNotificationToDatabase(
        id: String,
        path: String,
        instance: ArrayList<NotificationDTO>
    ) {
        try {
            val ref = database.child(path).child(id).child(path)
            setValue(ref, instance.map { it.toMap() })
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override suspend fun deleteNotificationFromDatabase(
        id: String,
        path: String,
        notification: NotificationDTO
    ) {
        try {
            val ref = database.child(path).child(id).child(path)
            val snapshot = getValue(ref)
            val list = (snapshot as? List<*>)?.mapNotNull { it as? Map<String, Any> }
                ?.mapNotNull { NotificationDTO.fromMap(it) }?.toMutableList()
            list?.remove(notification)
            setValue(ref, list!!.map { it.toMap() })
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override suspend fun updateIsReadStatusOfNotification(
        userId: String,
        notificationId: String,
        userPath: String,
        notificationPath: String
    ) {
        val ref = FIRDatabase.database().reference()
            .child(userPath).child(userId).child(notificationPath)
        ref.observeSingleEventOfType(FIRDataEventType.FIRDataEventTypeValue, withBlock = { snapshot ->
            if (snapshot != null && snapshot.exists()) {
                val children = snapshot.children
                while (true) {
                    val child = children.nextObject() as? FIRDataSnapshot ?: break
                    val id = (child.value as? Map<*, *>)?.get("id") as? String ?: continue
                    if (id == notificationId) {
                        child.ref.child("beRead").setValue(true) { _, _ -> }
                        break
                    }
                }
            }
        }) { _ -> }
    }

    override suspend fun deleteAllNotifications(
        uid: String,
        userPath: String,
        notificationPath: String
    ): Result<Unit> = suspendCancellableCoroutine { cont ->
        val ref = FIRDatabase.database().reference()
            .child(userPath).child(uid).child(notificationPath)
        ref.removeValueWithCompletionBlock { error, _ ->
            if (cont.isActive) {
                if (error == null) cont.resume(Result.success(Unit))
                else cont.resume(Result.failure(Exception(error.localizedDescription)))
            }
        }
    }

    private suspend fun setValue(ref: FIRDatabaseReference, value: Any): Boolean =
        suspendCancellableCoroutine { cont ->
            val preparedValue = prepareValueForFirebase(value)
            ref.setValue(preparedValue) { error, _ ->
                if (error == null) cont.resume(true)
                else cont.resumeWithException(Throwable(error.localizedDescription))
            }
        }

    private suspend fun getValue(ref: FIRDatabaseReference): Any? =
        suspendCancellableCoroutine { cont ->
            ref.observeSingleEventOfType(
                FIRDataEventType.FIRDataEventTypeValue
            ) { snapshot: FIRDataSnapshot?, previousSiblingKey: String? ->
                if (snapshot != null) {
                    cont.resume(snapshot.value)
                } else {
                    cont.resume(null)
                }
            }
        }

    @OptIn(BetaInteropApi::class)
    private fun prepareValueForFirebase(value: Any): Any {
        return when (value) {
            is Map<*, *> -> {
                val dict = NSMutableDictionary()
                value.forEach { (key, v) ->
                    if (key is String && v != null) {
                        dict.setObject(prepareValueForFirebase(v), key.toNSString())
                    }
                }
                dict
            }
            is List<*> -> {
                val array = NSMutableArray()
                value.forEach { item ->
                    if (item != null) {
                        array.addObject(prepareValueForFirebase(item))
                    }
                }
                array
            }
            is Boolean -> NSNumber.numberWithBool(value)
            is Int -> NSNumber.numberWithInt(value)
            is Double -> NSNumber.numberWithDouble(value)
            is Float -> NSNumber.numberWithFloat(value)
            is String -> NSString.create(string = value)
            else -> value
        }
    }

    @OptIn(BetaInteropApi::class)
    private fun String.toNSString(): NSString = NSString.create(string = this)
}
