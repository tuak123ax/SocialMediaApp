package com.minhtu.firesocialmedia.data.remote.service.database

import cocoapods.FirebaseDatabase.FIRDataEventType
import cocoapods.FirebaseDatabase.FIRDatabase
import cocoapods.FirebaseDatabase.FIRDatabaseReference
import com.minhtu.firesocialmedia.data.remote.dto.user.UserDTO
import com.minhtu.firesocialmedia.data.remote.dto.user.toMap
import com.minhtu.firesocialmedia.constants.auth.Constants
import com.minhtu.firesocialmedia.storage.auth.SupabaseStorageProvider
import com.minhtu.firesocialmedia.constants.auth.DataConstant
import com.minhtu.firesocialmedia.data.remote.dto.settings.auth.IpInfoResponseDTO
import com.minhtu.firesocialmedia.ios.service.serviceimpl.crypto.IosCryptoHelper
import com.minhtu.firesocialmedia.ios.service.serviceimpl.database.supabase.auth.SupabaseStorage
import com.minhtu.firesocialmedia.ios.service.serviceimpl.database.supabase.SupabaseStorageHelper
import com.minhtu.firesocialmedia.platform.getCurrentTime
import com.minhtu.firesocialmedia.platform.logMessage
import kotlinx.cinterop.BetaInteropApi
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
import platform.Foundation.NSUUID
import platform.UIKit.UIDevice
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi

private fun Map<*, *>.toAuthUserDTO(): UserDTO {
    val likedPosts = (this["likedPosts"] as? Map<*, *>)?.mapNotNull { entry ->
        val key = entry.key as? String
        val value = when (val rawValue = entry.value) {
            is Number -> rawValue.toInt()
            else -> null
        }
        if (key != null && value != null) key to value else null
    }?.toMap()?.let { HashMap(it) } ?: HashMap()

    val likedComments = (this["likedComments"] as? Map<*, *>)?.mapNotNull { entry ->
        val key = entry.key as? String
        val value = when (val rawValue = entry.value) {
            is Number -> rawValue.toInt()
            else -> null
        }
        if (key != null && value != null) key to value else null
    }?.toMap()?.let { HashMap(it) } ?: HashMap()

    val friendRequests = (this["friendRequests"] as? List<*>)?.mapNotNull { it as? String }
        ?.let { ArrayList(it) } ?: ArrayList()

    val friends = (this["friends"] as? List<*>)?.mapNotNull { it as? String }
        ?.let { ArrayList(it) } ?: ArrayList()

    return UserDTO(
        email = this["email"] as? String ?: "",
        image = this["image"] as? String ?: "",
        name = this["name"] as? String ?: "",
        status = this["status"] as? String ?: "",
        token = this["token"] as? String ?: "",
        uid = this["uid"] as? String ?: "",
        likedPosts = likedPosts,
        friendRequests = friendRequests,
        friends = friends,
        likedComments = likedComments
    )
}

class IosAuthDatabaseService : AuthDatabaseService {
    override suspend fun getUser(userId: String): UserDTO? {
        val rawUser = suspendCancellableCoroutine<UserDTO?> { continuation ->
            val databaseReference = FIRDatabase.database().reference()
                .child(DataConstant.USER_PATH)
                .child(userId)

            databaseReference.observeSingleEventOfType(
                FIRDataEventType.FIRDataEventTypeValue,
                withBlock = { snapshot ->
                    if (snapshot != null && snapshot.exists()) {
                        val value = snapshot.value as? Map<*, *>
                        if (value != null) {
                            try {
                                continuation.resume(value.toAuthUserDTO()) {}
                            } catch (_: Exception) {
                                continuation.resume(null) {}
                            }
                        } else {
                            continuation.resume(null) {}
                        }
                    } else {
                        continuation.resume(null) {}
                    }
                }
            ) { _ -> continuation.resume(null) {} }
        } ?: return null

        if (rawUser.image.isNotEmpty()) rawUser.updateImage(SupabaseStorageHelper.resolveMediaUrlAsync(rawUser.image))
        return rawUser
    }

    @OptIn(ExperimentalEncodingApi::class)
    override suspend fun saveSignUpInformation(user: UserDTO): Boolean {
        val dbRef = FIRDatabase.database().reference()
            .child(DataConstant.USER_PATH)
            .child(user.uid)
        return try {
            val shouldUploadAvatar =
                user.image != SupabaseStorageProvider.DEFAULT_AVATAR_URL &&
                        user.image != SupabaseStorageProvider.DEFAULT_DECADE_AVATAR_URL &&
                        user.image != SupabaseStorageProvider.DEFAULT_GROUP_AVATAR_URL

            if (shouldUploadAvatar) {
                val bytes = Base64.Default.decode(user.image)
                val remotePath = "avatar/${user.uid}_${getCurrentTime()}.jpg"
                SupabaseStorage.uploadBytes(bytes, remotePath)
                user.updateImage(remotePath)
            }
            setValueSuspend(dbRef, user.toMap())
            true
        } catch (e: Exception) {
            logMessage("saveSignUpInformation", { "Exception: ${e.message}" })
            false
        }
    }

    private suspend fun setValueSuspend(ref: FIRDatabaseReference, value: Map<String, Any?>) =
        suspendCancellableCoroutine<Unit> { cont ->
            ref.setValue(prepareForFirebase(value)) { error, _ ->
                if (error == null) cont.resume(Unit)
                else cont.resumeWithException(Throwable(error.localizedDescription))
            }
        }

    @OptIn(BetaInteropApi::class)
    private fun prepareForFirebase(value: Any?): Any? {
        return when (value) {
            null -> null
            is Map<*, *> -> {
                val dict = NSMutableDictionary()
                value.forEach { (k, v) ->
                    if (k is String) {
                        prepareForFirebase(v)?.let { dict.setObject(it, NSString.create(string = k)) }
                    }
                }
                dict
            }
            is List<*> -> {
                val arr = NSMutableArray()
                value.forEach { item -> prepareForFirebase(item)?.let { arr.addObject(it) } }
                arr
            }
            is Boolean -> NSNumber.numberWithBool(value)
            is Int -> NSNumber.numberWithInt(value)
            is Long -> NSNumber.numberWithDouble(value.toDouble())
            is Double -> NSNumber.numberWithDouble(value)
            is Float -> NSNumber.numberWithFloat(value)
            is String -> NSString.create(string = value)
            else -> value
        }
    }

    override suspend fun saveLoginActivityInfo(
        userId: String,
        locationInfo: IpInfoResponseDTO,
        historyPath: String,
        loginHistoryPath: String
    ) {
        try {
            val sessionId = NSUUID.UUID().UUIDString()
            IosCryptoHelper.saveToKeychain(Constants.KEY_SESSION_ID, sessionId)
            val deviceName = UIDevice.currentDevice.name
            val location = locationInfo.locationInfo()
            val timeMillis = getCurrentTime()
            val sessionMap = mapOf<Any?, Any?>(
                "sessionId" to sessionId,
                "deviceName" to deviceName,
                "location" to location,
                "time" to timeMillis,
                "status" to "ACTIVE"
            )
            val ref = FIRDatabase.database().reference()
                .child(historyPath).child(loginHistoryPath).child(userId).child(sessionId)
            suspendCancellableCoroutine<Unit> { cont ->
                ref.setValue(sessionMap) { _, _ -> if (cont.isActive) cont.resume(Unit) {} }
            }
        } catch (e: Exception) {
            logMessage("saveLoginActivityInfo", { "Exception: ${e.message}" })
        }
    }
}
