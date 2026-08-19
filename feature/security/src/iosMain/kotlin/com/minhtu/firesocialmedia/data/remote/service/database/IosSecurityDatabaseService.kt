package com.minhtu.firesocialmedia.data.remote.service.database

import cocoapods.FirebaseDatabase.FIRDataEventType
import cocoapods.FirebaseDatabase.FIRDataSnapshot
import cocoapods.FirebaseDatabase.FIRDatabase
import cocoapods.FirebaseDatabase.FIRDatabaseReference
import com.minhtu.firesocialmedia.constants.security.Constants
import com.minhtu.firesocialmedia.data.remote.dto.settings.security.SessionItemDTO
import com.minhtu.firesocialmedia.ios.service.serviceimpl.crypto.IosCryptoHelper
import com.minhtu.firesocialmedia.ios.service.serviceimpl.database.security.IosDatabaseHelper
import com.minhtu.firesocialmedia.platform.logMessage
import kotlinx.coroutines.suspendCancellableCoroutine

class IosSecurityDatabaseService : SecurityDatabaseService {
    private var sessionStatusHandle: ULong? = null
    private var sessionStatusRef: FIRDatabaseReference? = null

    override suspend fun fetchLoginHistoryList(
        userId: String,
        historyPath: String,
        loginHistoryPath: String
    ): List<SessionItemDTO> = suspendCancellableCoroutine { cont ->
        val ref = FIRDatabase.database().reference()
            .child(historyPath).child(loginHistoryPath).child(userId)
        ref.observeSingleEventOfType(FIRDataEventType.FIRDataEventTypeValue, withBlock = { snapshot ->
            val result = mutableListOf<SessionItemDTO>()
            if (snapshot != null && snapshot.exists()) {
                val children = snapshot.children
                while (true) {
                    val child = children.nextObject() as? FIRDataSnapshot ?: break
                    val map = child.value as? Map<*, *> ?: continue
                    val sessionId = child.key ?: ""
                    result.add(SessionItemDTO(
                        sessionId = map["sessionId"] as? String ?: sessionId,
                        deviceName = map["deviceName"] as? String ?: "",
                        location = map["location"] as? String ?: "",
                        time = (map["time"] as? Long) ?: 0L,
                        status = map["status"] as? String ?: ""
                    ))
                }
            }
            if (cont.isActive) cont.resume(result) {}
        }) { _ -> if (cont.isActive) cont.resume(emptyList()) {} }
    }

    override fun getLocalSessionId(): String {
        return IosCryptoHelper.getFromKeychain(Constants.KEY_SESSION_ID) ?: ""
    }

    override fun clearLocalSessionId() {
        IosCryptoHelper.saveToKeychain(Constants.KEY_SESSION_ID, "")
    }

    override fun observeSessionStatus(
        userId: String,
        sessionId: String,
        historyPath: String,
        loginHistoryPath: String,
        onLoggedOut: () -> Unit
    ) {
        stopObserveSessionStatus()
        if (sessionId.isEmpty()) return
        val ref = FIRDatabase.database().reference()
            .child(historyPath).child(loginHistoryPath).child(userId).child(sessionId).child("status")
        sessionStatusRef = ref
        sessionStatusHandle = ref.observeEventType(
            FIRDataEventType.FIRDataEventTypeValue,
            withBlock = { snapshot ->
                val status = snapshot?.value as? String ?: return@observeEventType
                if (status == "LOGOUT") onLoggedOut()
            }
        )
    }

    override fun stopObserveSessionStatus() {
        val handle = sessionStatusHandle
        val ref = sessionStatusRef
        if (handle != null && ref != null) {
            ref.removeObserverWithHandle(handle)
        }
        sessionStatusRef = null
        sessionStatusHandle = null
    }

    override suspend fun deleteLoginSession(
        userId: String,
        sessionId: String,
        historyPath: String,
        loginHistoryPath: String
    ): Boolean = suspendCancellableCoroutine { cont ->
        val ref = FIRDatabase.database().reference()
            .child(historyPath).child(loginHistoryPath).child(userId).child(sessionId)
        ref.removeValueWithCompletionBlock { error, _ ->
            if (cont.isActive) cont.resume(error == null) {}
        }
    }

    override suspend fun logoutSession(
        userId: String,
        sessionId: String,
        historyPath: String,
        loginHistoryPath: String
    ): Boolean = suspendCancellableCoroutine { cont ->
        val ref = FIRDatabase.database().reference()
            .child(historyPath).child(loginHistoryPath).child(userId).child(sessionId).child("status")
        ref.setValue("LOGOUT") { error, _ ->
            if (cont.isActive) cont.resume(error == null) {}
        }
    }

    override suspend fun downloadImage(image: String, fileName: String): Boolean {
        return IosDatabaseHelper.downloadImage(image, fileName)
    }

    override suspend fun updateTwoFAEnabledFlagForUser(
        userId: String,
        twoFAEnabled: Boolean,
        userPath: String,
        twoFaEnabledPath: String
    ): Boolean = suspendCancellableCoroutine { cont ->
        val ref = FIRDatabase.database().reference()
            .child(userPath).child(userId).child(twoFaEnabledPath)
        ref.setValue(twoFAEnabled) { error, _ ->
            if (cont.isActive) cont.resume(error == null) {}
        }
    }

    override suspend fun updateUserLongField(
        userId: String,
        fieldPath: String,
        value: Long,
        userPath: String
    ): Boolean = suspendCancellableCoroutine { cont ->
        val ref = FIRDatabase.database().reference()
            .child(userPath).child(userId).child(fieldPath)
        ref.setValue(value) { error, _ ->
            if (cont.isActive) cont.resume(error == null) {}
        }
    }
}
