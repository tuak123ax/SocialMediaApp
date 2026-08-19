package com.minhtu.firesocialmedia.ios.service.serviceimpl.auth

import cocoapods.FirebaseAuth.FIRAuth
import cocoapods.FirebaseAuth.FIREmailAuthProvider
import cocoapods.FirebaseDatabase.FIRDatabase
import com.minhtu.firesocialmedia.data.remote.service.auth.security.AuthException
import com.minhtu.firesocialmedia.data.remote.service.auth.SecurityAuthService
import com.minhtu.firesocialmedia.platform.getCurrentTime
import com.minhtu.firesocialmedia.platform.logMessage
import kotlinx.coroutines.delay
import kotlinx.coroutines.suspendCancellableCoroutine

class IosSecurityAuthService : SecurityAuthService {
    override suspend fun reAuthenticate(
        currentUserEmail: String,
        currentPassword: String
    ): Boolean = suspendCancellableCoroutine { cont ->
        val user = FIRAuth.auth().currentUser() ?: run {
            cont.resume(false) {}
            return@suspendCancellableCoroutine
        }
        val credential = FIREmailAuthProvider.credentialWithEmail(
            currentUserEmail,
            password = currentPassword
        )
        user.reauthenticateWithCredential(credential) { _, error ->
            if (cont.isActive) cont.resume(error == null) {}
        }
    }

    override suspend fun changePassword(
        userId: String,
        newPassword: String,
        userPath: String,
        lastTimeChangePasswordPath: String
    ): Result<Unit> {
        val user = FIRAuth.auth().currentUser()
            ?: return Result.failure(AuthException("USER_NOT_LOGIN", "User not logged in"))

        val updateResult = suspendCancellableCoroutine<Boolean> { cont ->
            user.updatePassword(newPassword) { error ->
                if (cont.isActive) cont.resume(error == null) {}
            }
        }
        if (!updateResult) {
            return Result.failure(AuthException("UNKNOWN", "Failed to update password"))
        }

        // Update DB with retry
        val dbRef = FIRDatabase.database().reference()
            .child(userPath)
            .child(userId)
            .child(lastTimeChangePasswordPath)

        var attempt = 0
        var delayTime = 200L
        var dbSuccess = false
        while (attempt < 3 && !dbSuccess) {
            try {
                dbSuccess = suspendCancellableCoroutine { cont ->
                    dbRef.setValue(getCurrentTime()) { error, _ ->
                        if (cont.isActive) cont.resume(error == null) {}
                    }
                }
            } catch (_: Exception) {}
            if (!dbSuccess) {
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
        return Result.success(Unit)
    }
}
