package com.minhtu.firesocialmedia.domain.serviceimpl.auth

import android.content.Context
import com.google.android.gms.auth.api.identity.SignInCredential
import com.google.firebase.Firebase
import com.google.firebase.FirebaseException
import com.google.firebase.FirebaseNetworkException
import com.google.firebase.FirebaseTooManyRequestsException
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthException
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.auth.FirebaseAuthMultiFactorException
import com.google.firebase.auth.FirebaseAuthRecentLoginRequiredException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseAuthWeakPasswordException
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.auth
import com.google.firebase.database.FirebaseDatabase
import com.minhtu.firesocialmedia.constants.Constants
import com.minhtu.firesocialmedia.domain.entity.authentication.TwoFARequest
import com.minhtu.firesocialmedia.domain.entity.authentication.TwoFAResponse
import com.minhtu.firesocialmedia.data.remote.dto.user.UserDTO
import com.minhtu.firesocialmedia.data.remote.service.auth.AuthService
import com.minhtu.firesocialmedia.domain.entity.forgotpassword.EmailExistResult
import com.minhtu.firesocialmedia.domain.entity.settings.ChangePasswordState
import com.minhtu.firesocialmedia.domain.error.changepassword.ChangePasswordError
import com.minhtu.firesocialmedia.domain.error.signin.SignInError
import com.minhtu.firesocialmedia.domain.error.signup.SignUpError
import com.minhtu.firesocialmedia.platform.AppConfig
import com.minhtu.firesocialmedia.platform.logMessage
import com.minhtu.firesocialmedia.platform.send2FARequest
import kotlinx.coroutines.delay
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.tasks.await
import java.security.SecureRandom
import kotlin.coroutines.cancellation.CancellationException
import kotlin.math.log

class AndroidAuthService(var context: Context) : AuthService{
    override suspend fun signInWithEmailAndPassword(
        email: String,
        password: String
    ): SignInError? {
        return try {
            FirebaseAuth.getInstance()
                .signInWithEmailAndPassword(email, password)
                .await() // suspend until complete
            null
        } catch (e: CancellationException) {
            SignInError.Unknown(e.message ?: "Unknown error")
        } catch (e: FirebaseAuthInvalidCredentialsException) {
            // Wrong password OR malformed email, inspect errorCode if you want
            return when (e.errorCode) {
                "ERROR_INVALID_EMAIL"   -> SignInError.InvalidEmail
                "ERROR_WRONG_PASSWORD"  -> SignInError.WrongPassword
                else                    -> SignInError.InvalidCredentials
            }
        } catch (e: FirebaseAuthInvalidUserException) {
            // User disabled / not found
            return when (e.errorCode) {
                "ERROR_USER_DISABLED"   -> SignInError.UserDisabled
                "ERROR_USER_NOT_FOUND"  -> SignInError.UserNotFound
                else                    -> SignInError.InvalidUser
            }
        } catch (e: FirebaseTooManyRequestsException) {
            return SignInError.TooManyRequests
        } catch (e: FirebaseNetworkException) {
            return SignInError.NetworkError
        } catch (e: FirebaseAuthMultiFactorException) {
            return SignInError.MultiFactor
        } catch (e: FirebaseException) {
            // Any other Firebase auth error
            return SignInError.Unknown(e.message ?: "Unknown error")
        }
    }

    override suspend fun signUpWithEmailAndPassword(
        email: String,
        password: String
    ): Result<Unit> {
        return try {
            FirebaseAuth.getInstance()
                .createUserWithEmailAndPassword(email, password)
                .await()

            Result.success(Unit)

        } catch (e: FirebaseAuthWeakPasswordException) {
            //Password too weak
            Result.failure(SignUpError.WeakPassword)

        } catch (e: FirebaseAuthInvalidCredentialsException) {
            //Invalid email format
            Result.failure(SignUpError.InvalidEmail)

        } catch (e: FirebaseAuthUserCollisionException) {
            //Email already exists
            Result.failure(SignUpError.EmailAlreadyInUse)

        } catch (e: FirebaseNetworkException) {
            //No internet
            Result.failure(SignUpError.NetworkError)

        } catch (e: FirebaseAuthException) {
            // any other Firebase auth error
            Result.failure(SignUpError.Unknown(e.message ?: "Authentication error"))

        } catch (e: Exception) {
            // truly unexpected
            Result.failure(SignUpError.Unknown(e.message ?: "Unknown error"))
        }
    }


    override suspend fun getCurrentUserUid(): String? = suspendCancellableCoroutine{ continuation ->
        if(continuation.isActive) continuation.resume(FirebaseAuth.getInstance().uid, onCancellation = {})
    }

    override suspend fun getCurrentUserEmail(): String? = suspendCancellableCoroutine{ continuation ->
        if(continuation.isActive) continuation.resume(FirebaseAuth.getInstance().currentUser?.email.toString(), onCancellation = {})
    }

    override suspend fun fetchSignInMethodsForEmail(email: String) : EmailExistResult = suspendCancellableCoroutine{ continuation ->
        FirebaseAuth.getInstance().fetchSignInMethodsForEmail(email)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val signInMethods = task.result?.signInMethods
                    if (signInMethods.isNullOrEmpty()) {
                        if(continuation.isActive)
                            continuation.resume(EmailExistResult(false, Constants.EMAIL_NOT_EXISTED),
                                onCancellation = {})
                    } else {
                        continuation.resume(EmailExistResult(true, Constants.EMAIL_EXISTED),
                            onCancellation = {})
                    }
                } else {
                    continuation.resume(EmailExistResult(false, Constants.EMAIL_SERVER_ERROR),
                        onCancellation = {})
                }
            }
    }

    override suspend fun sendPasswordResetEmail(email: String) : Boolean = suspendCancellableCoroutine { continuation ->
        FirebaseAuth.getInstance().sendPasswordResetEmail(email)
            .addOnCompleteListener{ task ->
                if(task.isSuccessful) {
                    if(continuation.isActive) continuation.resume(true, onCancellation = {})
                } else {
                    continuation.resume(true, onCancellation = {})
                }
            }
    }

    override suspend fun handleSignInGoogleResult(credential: Any) : String? =
        suspendCancellableCoroutine{ continuation ->
            val idToken = (credential as SignInCredential).googleIdToken
            when {
                idToken != null -> {
                    // Got an ID token from Google. Use it to authenticate
                    // with Firebase.
                    val firebaseCredential = GoogleAuthProvider.getCredential(idToken, null)
                    Firebase.auth.signInWithCredential(firebaseCredential)
                        .addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                val user = Firebase.auth.currentUser
                                if(user != null && continuation.isActive) {
                                    continuation.resume(
                                        user.email,
                                        onCancellation = {
                                        })
                                }
                            } else {
                                continuation.resume(
                                    null,
                                    onCancellation = {
                                    })
                            }
                        }
                }
                else -> {
                    // Shouldn't happen.
                    logMessage("Signin", { "No ID token!" })
                    continuation.resume(
                        null,
                        onCancellation = {
                        })
                }
            }
        }

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
        userDTO: UserDTO,
        newPassword: String,
        userPath: String,
        lastTimeChangePasswordPath: String): ChangePasswordState {
        val user = FirebaseAuth.getInstance().currentUser
            ?: return ChangePasswordState(false, ChangePasswordError.UserNotLoginError)

        val databaseRef = FirebaseDatabase
            .getInstance()
            .reference
            .child(userPath)
            .child(userDTO.uid)
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

            // Accept inconsistency if still fails
            if (!dbSuccess) {
                 logMessage("changePassword",
                     { "Failed to update DB after retries for user: ${userDTO.uid}" })
            }

            ChangePasswordState(true)

        } catch (_: FirebaseAuthRecentLoginRequiredException) {
            ChangePasswordState(false, ChangePasswordError.ReauthenticateRequiredError)

        } catch (e: Exception) {
            ChangePasswordState(false, ChangePasswordError.Unknown(e.message ?: ""))
        }
    }

    private val BASE32_CHARS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ234567"
    private val secureRandom = SecureRandom()

    override suspend fun generateSecretFor2FA(): String {
        val length = 16
        val result = StringBuilder(length)
        repeat(length) {
            val index = secureRandom.nextInt(BASE32_CHARS.length)
            result.append(BASE32_CHARS[index])
        }
        return result.toString()
    }

    override suspend fun enableOTP(userId : String,
                                   secret : String,
                                   otpToVerify: String) : TwoFAResponse {
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
        } catch(e : Exception) {
            logMessage("enableOTP", { "Exception happened: " + e.message })
            TwoFAResponse(false, "Exception happened!")
        }
    }

    override suspend fun verifyOTP(userId : String,
                                   otpToVerify: String) : TwoFAResponse {
        return try{
            send2FARequest(
                TwoFARequest(
                    apiKey = AppConfig.twoFAApiKey,
                    action = "verify",
                    userId = userId,
                    otp = otpToVerify
                )
            )
        } catch (e : Exception) {
            logMessage("verifyOTP", { "Exception happened: " + e.message })
            TwoFAResponse(false, "Exception happened!")
        }
    }

    override suspend fun disable2FA(userId: String) : TwoFAResponse {
        return try{
            send2FARequest(
                TwoFARequest(
                    apiKey = AppConfig.twoFAApiKey,
                    action = "disable",
                    userId = userId
                )
            )
        } catch (e : Exception) {
            logMessage("disable2FA", { "Exception happened: " + e.message })
            TwoFAResponse(false, "Exception happened!")
        }
    }

    override suspend fun verifyBackupCode(
        userId: String,
        backupCode: String
    ): TwoFAResponse {
        return try{
            send2FARequest(
                TwoFARequest(
                    apiKey = AppConfig.twoFAApiKey,
                    action = "verify_backup",
                    userId = userId,
                    backupCode = backupCode
                )
            )
        } catch (e : Exception) {
            logMessage("verifyBackupCode", { "Exception happened: " + e.message })
            TwoFAResponse(false, "Exception happened!")
        }
    }
}