package com.minhtu.firesocialmedia.domain.serviceimpl.auth

import cocoapods.FirebaseAuth.FIRAuth
import cocoapods.FirebaseDatabase.FIRDatabase
import com.minhtu.firesocialmedia.data.remote.dto.user.UserDTO
import com.minhtu.firesocialmedia.data.remote.service.auth.AuthService
import com.minhtu.firesocialmedia.core.domain.entity.authentication.TwoFARequest
import com.minhtu.firesocialmedia.core.domain.entity.authentication.TwoFAResponse
import com.minhtu.firesocialmedia.core.domain.entity.forgotpassword.EmailExistResult
import com.minhtu.firesocialmedia.core.domain.entity.settings.ChangePasswordState
import com.minhtu.firesocialmedia.core.domain.error.changepassword.ChangePasswordError
import com.minhtu.firesocialmedia.core.domain.error.signin.SignInError
import com.minhtu.firesocialmedia.platform.AppConfig
import com.minhtu.firesocialmedia.platform.logMessage
import com.minhtu.firesocialmedia.platform.send2FARequest
import com.minhtu.firesocialmedia.core.constants.Constants
import com.minhtu.firesocialmedia.platform.getCurrentTime
import kotlinx.coroutines.delay
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

@Suppress("UNCHECKED_CAST")
class IosAuthService() : AuthService{
    override suspend fun signInWithEmailAndPassword(
        email: String,
        password: String
    ): SignInError? {
        return suspendCancellableCoroutine { cont ->
            FIRAuth.auth().signInWithEmail(email, password = password) { authResult, error ->
                if (error != null) {
                    com.minhtu.firesocialmedia.platform.logMessage("iOSAuth") { "signIn error: ${error.localizedDescription}" }
                    cont.resume(SignInError.Unknown(error.localizedDescription))
                } else {
                    com.minhtu.firesocialmedia.platform.logMessage("iOSAuth") { "signIn success" }
                    cont.resume(null)
                }
            }
        }
    }

    override suspend fun signUpWithEmailAndPassword(
        email: String,
        password: String
    ): Result<Unit> {
        return suspendCancellableCoroutine { cont ->
            FIRAuth.auth().createUserWithEmail(email, password = password) { authResult, error ->
                if (error != null) {
                    cont.resume(Result.failure(SignInError.Unknown(error.localizedDescription ?: "Unknown error")),
                        onCancellation = {})
                } else {
                    cont.resume(Result.success(Unit),
                        onCancellation = {})
                }
            }
        }
    }

    override suspend fun getCurrentUserUid(): String? {
        return FIRAuth.auth().currentUser()?.uid()
    }

    override suspend fun getCurrentUserEmail(): String? {
        return FIRAuth.auth().currentUser()?.email()
    }

    override suspend fun fetchSignInMethodsForEmail(email: String): EmailExistResult = suspendCancellableCoroutine { continuation ->
        FIRAuth.auth().fetchSignInMethodsForEmail(email) { result, error ->
            if (error != null || result == null) {
                if(continuation.isActive) continuation.resume(EmailExistResult(false, Constants.EMAIL_SERVER_ERROR))
            } else {
                if (result.isNotEmpty()) {
                    if(continuation.isActive) continuation.resume(EmailExistResult(true, Constants.EMAIL_EXISTED))
                } else {
                    if(continuation.isActive) continuation.resume(EmailExistResult(false, Constants.EMAIL_NOT_EXISTED))
                }
            }
        }
    }

    override suspend fun sendPasswordResetEmail(email: String): Boolean = suspendCancellableCoroutine{ continuation ->
        FIRAuth.auth().sendPasswordResetWithEmail(email) { error ->
            if(error == null) {
                if(continuation.isActive) continuation.resume(true)
            } else {
                if(continuation.isActive) continuation.resume(false)
            }
        }
    }

    override suspend fun handleSignInGoogleResult(credentials: Any): String? {
        //Not yet implemented
        return ""
    }

    override suspend fun reAuthenticate(
        currentUserEmail: String,
        currentPassword: String
    ): Boolean = suspendCancellableCoroutine { cont ->
        val user = FIRAuth.auth().currentUser() ?: run {
            cont.resume(false)
            return@suspendCancellableCoroutine
        }
        val credential = cocoapods.FirebaseAuth.FIREmailAuthProvider.credentialWithEmail(
            currentUserEmail,
            password = currentPassword
        )
        user.reauthenticateWithCredential(credential) { _, error ->
            if (cont.isActive) cont.resume(error == null)
        }
    }

    override suspend fun changePassword(
        userDTO: UserDTO,
        newPassword: String,
        userPath: String,
        lastTimeChangePasswordPath: String
    ): ChangePasswordState {
        val user = FIRAuth.auth().currentUser()
            ?: return ChangePasswordState(false, ChangePasswordError.UserNotLoginError)

        val updateResult = suspendCancellableCoroutine<Boolean> { cont ->
            user.updatePassword(newPassword) { error ->
                if (cont.isActive) cont.resume(error == null)
            }
        }
        if (!updateResult) {
            return ChangePasswordState(false, ChangePasswordError.Unknown("Failed to update password"))
        }

        // Update DB with retry
        val dbRef = FIRDatabase.database().reference()
            .child(userPath)
            .child(userDTO.uid)
            .child(lastTimeChangePasswordPath)

        var attempt = 0
        var delayTime = 200L
        var dbSuccess = false
        while (attempt < 3 && !dbSuccess) {
            try {
                dbSuccess = suspendCancellableCoroutine { cont ->
                    dbRef.setValue(getCurrentTime()) { error, _ ->
                        if (cont.isActive) cont.resume(error == null)
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
            logMessage("changePassword", { "Failed to update DB after retries for user: ${userDTO.uid}" })
        }
        return ChangePasswordState(true)
    }

    private val BASE32_CHARS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ234567"

    override suspend fun generateSecretFor2FA(): String {
        val length = 16
        val result = StringBuilder(length)
        repeat(length) {
            result.append(BASE32_CHARS[(0 until BASE32_CHARS.length).random()])
        }
        return result.toString()
    }

    override suspend fun enableOTP(userId: String, secret: String, otpToVerify: String): TwoFAResponse {
        return try {
            send2FARequest(
                TwoFARequest(
                    apiKey = AppConfig.twoFAApiKey,
                    action = "enable",
                    userId = userId,
                    secret = secret,
                    otp = otpToVerify
                )
            )
        } catch (e: Exception) {
            logMessage("enableOTP", { "Exception happened: ${e.message}" })
            TwoFAResponse(false, "Exception happened!")
        }
    }

    override suspend fun verifyOTP(userId: String, otpToVerify: String): TwoFAResponse {
        return try {
            send2FARequest(
                TwoFARequest(
                    apiKey = AppConfig.twoFAApiKey,
                    action = "verify",
                    userId = userId,
                    otp = otpToVerify
                )
            )
        } catch (e: Exception) {
            logMessage("verifyOTP", { "Exception happened: ${e.message}" })
            TwoFAResponse(false, "Exception happened!")
        }
    }

    override suspend fun disable2FA(userId: String): TwoFAResponse {
        return try {
            send2FARequest(
                TwoFARequest(
                    apiKey = AppConfig.twoFAApiKey,
                    action = "disable",
                    userId = userId
                )
            )
        } catch (e: Exception) {
            logMessage("disable2FA", { "Exception happened: ${e.message}" })
            TwoFAResponse(false, "Exception happened!")
        }
    }

    override suspend fun verifyBackupCode(userId: String, backupCode: String): TwoFAResponse {
        return try {
            send2FARequest(
                TwoFARequest(
                    apiKey = AppConfig.twoFAApiKey,
                    action = "verify_backup",
                    userId = userId,
                    backupCode = backupCode
                )
            )
        } catch (e: Exception) {
            logMessage("verifyBackupCode", { "Exception happened: ${e.message}" })
            TwoFAResponse(false, "Exception happened!")
        }
    }
}