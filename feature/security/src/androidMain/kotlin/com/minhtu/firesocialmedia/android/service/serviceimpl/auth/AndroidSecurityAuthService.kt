package com.minhtu.firesocialmedia.android.service.serviceimpl.auth

import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthRecentLoginRequiredException
import com.google.firebase.database.FirebaseDatabase
import com.minhtu.firesocialmedia.data.remote.service.auth.security.AuthException
import com.minhtu.firesocialmedia.data.remote.service.auth.SecurityAuthService
import com.minhtu.firesocialmedia.platform.logMessage
import kotlinx.coroutines.delay
import kotlinx.coroutines.tasks.await

class AndroidSecurityAuthService : SecurityAuthService {
    override suspend fun reAuthenticate(
        currentUserEmail: String,
        currentPassword: String
    ): Boolean {
        val user = FirebaseAuth.getInstance().currentUser ?: return false

        val credential = EmailAuthProvider.getCredential(currentUserEmail, currentPassword)

        return try {
            user.reauthenticate(credential).await()
            true
        } catch (_: Exception) {
            false
        }
    }

    override suspend fun changePassword(
        userId: String,
        newPassword: String,
        userPath: String,
        lastTimeChangePasswordPath: String
    ): Result<Unit> {
        val user = FirebaseAuth.getInstance().currentUser
            ?: return Result.failure(AuthException("USER_NOT_LOGIN", "User not logged in"))

        val databaseRef = FirebaseDatabase
            .getInstance()
            .reference
            .child(userPath)
            .child(userId)
            .child(lastTimeChangePasswordPath)

        return try {
            // STEP 1: Update Auth FIRST
            user.updatePassword(newPassword).await()

            // STEP 2: Update DB with retry (max 3 times)
            var attempt = 0
            var delayTime = 200L
            var dbSuccess = false

            while (attempt < 3 && !dbSuccess) {
                try {
                    databaseRef.setValue(System.currentTimeMillis()).await()
                    dbSuccess = true
                } catch (_: Exception) {
                    attempt++
                    if (attempt < 3) {
                        delay(delayTime)
                        delayTime = (delayTime * 2).coerceAtMost(1000L)
                    }
                }
            }

            if (!dbSuccess) {
                logMessage("changePassword", { "Failed to update DB after retries for user: $userId" })
            }

            Result.success(Unit)
        } catch (_: FirebaseAuthRecentLoginRequiredException) {
            Result.failure(AuthException("REAUTHENTICATE_REQUIRED", "Reauthenticate required"))
        } catch (e: Exception) {
            Result.failure(AuthException("UNKNOWN", e.message ?: ""))
        }
    }
}
