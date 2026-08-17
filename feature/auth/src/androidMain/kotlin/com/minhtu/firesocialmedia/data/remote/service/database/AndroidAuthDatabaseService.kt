package com.minhtu.firesocialmedia.data.remote.service.database

import android.content.Context
import android.os.Build
import android.util.Log
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.minhtu.firesocialmedia.android.service.serviceimpl.database.supabase.auth.SupabaseStorage
import com.minhtu.firesocialmedia.android.service.serviceimpl.database.supabase.SupabaseStorageHelper
import com.minhtu.firesocialmedia.android.service.serviceimpl.database.supabase.SupabaseStorageHelper.Companion.resolveMediaUrlAsync
import com.minhtu.firesocialmedia.auth.data.remote.dto.user.UserDTO
import com.minhtu.firesocialmedia.constants.auth.Constants
import com.minhtu.firesocialmedia.storage.auth.SupabaseStorageProvider
import com.minhtu.firesocialmedia.constants.auth.DataConstant
import com.minhtu.firesocialmedia.data.remote.dto.settings.auth.IpInfoResponseDTO
import com.minhtu.firesocialmedia.data.remote.dto.settings.auth.SessionItemDTO
import com.minhtu.firesocialmedia.platform.logMessage
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withTimeout
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

class AndroidAuthDatabaseService(context: Context) : AuthDatabaseService {
    // Never hold a Service context to avoid leaks; keep only applicationContext
    private val appContext: Context = context.applicationContext
    private val fileExtensionHelper = SupabaseStorageHelper()

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

    override suspend fun saveSignUpInformation(user: UserDTO): Boolean {
        val databaseReference = FirebaseDatabase.getInstance()
            .getReference()
            .child(DataConstant.USER_PATH)
            .child(user.uid)

        return try {
            val shouldUploadAvatar =
                user.image != SupabaseStorageProvider.DEFAULT_AVATAR_URL &&
                        user.image != SupabaseStorageProvider.DEFAULT_DECADE_AVATAR_URL &&
                        user.image != SupabaseStorageProvider.DEFAULT_GROUP_AVATAR_URL

            if (shouldUploadAvatar) {
                val extension = fileExtensionHelper.getFileExtension(user.image, "jpg")

                val remotePath =
                    "avatar/${user.uid}_${System.currentTimeMillis()}.$extension"

                SupabaseStorage.upload(
                    filePath = user.image,
                    remotePath = remotePath
                )

                user.updateImage(remotePath) // store relative path
            }

            databaseReference.setValue(user).await()
            true

        } catch (e: Exception) {
            Log.e("Task", "saveSignUpInformation failed: ${e.message}", e)
            false
        }
    }

    override suspend fun saveLoginActivityInfo(
        userId: String,
        locationInfo: IpInfoResponseDTO,
        historyPath: String,
        loginHistoryPath: String
    ) {
        try {
            val sessionItemDTO = prepareSessionItemData(locationInfo)
            val dbRef = FirebaseDatabase
                .getInstance()
                .reference
                .child(historyPath)
                .child(loginHistoryPath)
                .child(userId)

            // Use sessionId as the node key instead of a Firebase push key
            dbRef.child(sessionItemDTO.sessionId).setValue(sessionItemDTO).await()
        } catch (e: Exception) {
            logMessage("saveLoginActivityInfo", { "Exception happened: ${e.message}" })
        }
    }

    private fun prepareSessionItemData(locationInfo: IpInfoResponseDTO): SessionItemDTO {
        val sessionId = java.util.UUID.randomUUID().toString()
        saveLocalSessionId(sessionId)
        val deviceName = getDeviceName()
        val location = locationInfo.locationInfo()
        val timeMillis = System.currentTimeMillis()
        return SessionItemDTO(
            sessionId = sessionId,
            deviceName = deviceName,
            location = location,
            time = timeMillis
        )
    }

    private fun saveLocalSessionId(sessionId: String) {
        appContext.getSharedPreferences("session_prefs", Context.MODE_PRIVATE)
            .edit()
            .putString(Constants.KEY_SESSION_ID, sessionId)
            .apply()
    }

    private fun getDeviceName(): String {
        val manufacturer = Build.MANUFACTURER
        val model = Build.MODEL
        return if (model.startsWith(manufacturer, ignoreCase = true)) {
            model.replaceFirstChar { it.uppercase() }
        } else {
            "${manufacturer.replaceFirstChar { it.uppercase() }} $model"
        }
    }
}
