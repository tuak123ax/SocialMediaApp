package com.minhtu.firesocialmedia.data.remote.service.database

import android.content.Context
import android.util.Log
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.minhtu.firesocialmedia.android.service.serviceimpl.database.security.AndroidDatabaseHelper
import com.minhtu.firesocialmedia.constants.security.Constants
import com.minhtu.firesocialmedia.data.remote.dto.settings.security.SessionItemDTO
import com.minhtu.firesocialmedia.platform.logMessage
import kotlinx.coroutines.tasks.await

class AndroidSecurityDatabaseService(context: Context) : SecurityDatabaseService {
    // Never hold a Service context to avoid leaks; keep only applicationContext
    private val appContext: Context = context.applicationContext

    private var sessionStatusListener: ValueEventListener? = null
    private var sessionStatusRef: DatabaseReference? = null

    override suspend fun fetchLoginHistoryList(
        userId: String,
        historyPath: String,
        loginHistoryPath: String
    ): List<SessionItemDTO> {
        return runCatching {
            val ref = FirebaseDatabase
                .getInstance()
                .reference
                .child(historyPath)
                .child(loginHistoryPath)
                .child(userId)

            Log.d("fetchLoginHistoryList", "Fetching login history...")
            Log.d("fetchLoginHistoryList", "UserId: $userId")

            val snapshot = ref.get().await()

            Log.d("fetchLoginHistoryList", "Snapshot exists: ${snapshot.exists()}")
            Log.d("fetchLoginHistoryList", "Children count: ${snapshot.childrenCount}")

            val result = snapshot.children.mapNotNull { child ->
                Log.d("fetchLoginHistoryList", "Raw child key: ${child.key}")
                Log.d("fetchLoginHistoryList", "Raw value: ${child.value}")

                // The node key IS the sessionId — populate it from the key
                val item = child.getValue(SessionItemDTO::class.java)
                    ?.copy(sessionId = child.key ?: "")

                if (item == null) {
                    Log.e("fetchLoginHistoryList", "Failed to parse child: ${child.key}")
                } else {
                    Log.d("fetchLoginHistoryList", "Parsed item: $item")
                }

                item
            }

            Log.d("fetchLoginHistoryList", "Final list size: ${result.size}")

            result
        }.onFailure { e ->
            Log.e("fetchLoginHistoryList", "Error fetching login history", e)
        }.getOrElse {
            emptyList()
        }
    }

    override fun getLocalSessionId(): String {
        return appContext.getSharedPreferences("session_prefs", Context.MODE_PRIVATE)
            .getString(Constants.KEY_SESSION_ID, "") ?: ""
    }

    override fun clearLocalSessionId() {
        appContext.getSharedPreferences("session_prefs", Context.MODE_PRIVATE)
            .edit()
            .remove(Constants.KEY_SESSION_ID)
            .apply()
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
        sessionStatusRef = FirebaseDatabase.getInstance().reference
            .child(historyPath)
            .child(loginHistoryPath)
            .child(userId)
            .child(sessionId)
            .child("status")
        sessionStatusListener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val status = snapshot.getValue(String::class.java) ?: return
                if (status == "LOGOUT") {
                    onLoggedOut()
                }
            }
            override fun onCancelled(error: DatabaseError) {
                logMessage("observeSessionStatus", { "Cancelled: ${error.message}" })
            }
        }
        sessionStatusRef!!.addValueEventListener(sessionStatusListener!!)
    }

    override fun stopObserveSessionStatus() {
        sessionStatusRef?.let { ref ->
            sessionStatusListener?.let { ref.removeEventListener(it) }
        }
        sessionStatusRef = null
        sessionStatusListener = null
    }

    override suspend fun deleteLoginSession(
        userId: String,
        sessionId: String,
        historyPath: String,
        loginHistoryPath: String
    ): Boolean {
        return try {
            // sessionId is the node key — direct O(1) delete, no scanning needed
            FirebaseDatabase
                .getInstance()
                .reference
                .child(historyPath)
                .child(loginHistoryPath)
                .child(userId)
                .child(sessionId)
                .removeValue()
                .await()
            true
        } catch (e: Exception) {
            logMessage("deleteLoginSession", { "Exception: ${e.message}" })
            false
        }
    }

    override suspend fun logoutSession(
        userId: String,
        sessionId: String,
        historyPath: String,
        loginHistoryPath: String
    ): Boolean {
        return try {
            FirebaseDatabase
                .getInstance()
                .reference
                .child(historyPath)
                .child(loginHistoryPath)
                .child(userId)
                .child(sessionId)
                .child("status")
                .setValue("LOGOUT")
                .await()
            true
        } catch (e: Exception) {
            logMessage("logoutSession", { "Exception: ${e.message}" })
            false
        }
    }

    override suspend fun downloadImage(image: String, fileName: String): Boolean {
        return AndroidDatabaseHelper.downloadImage(appContext, image, fileName)
    }

    override suspend fun updateTwoFAEnabledFlagForUser(
        userId: String,
        twoFAEnabled: Boolean,
        userPath: String,
        twoFaEnabledPath: String
    ): Boolean {
        return AndroidDatabaseHelper.updateTwoFAEnabledFlagForUser(userId, twoFAEnabled, userPath, twoFaEnabledPath)
    }

    override suspend fun updateUserLongField(
        userId: String,
        fieldPath: String,
        value: Long,
        userPath: String
    ): Boolean {
        return AndroidDatabaseHelper.updateUserLongField(userId, fieldPath, value, userPath)
    }
}
