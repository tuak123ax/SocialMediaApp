package com.minhtu.firesocialmedia.domain.serviceimpl.auth

import android.app.Activity
import android.content.Context
import com.google.android.gms.auth.api.identity.SignInCredential
import com.google.firebase.Firebase
import com.google.firebase.FirebaseException
import com.google.firebase.FirebaseNetworkException
import com.google.firebase.FirebaseTooManyRequestsException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.auth.FirebaseAuthMultiFactorException
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.auth
import com.minhtu.firesocialmedia.constants.Constants
import com.minhtu.firesocialmedia.domain.entity.forgotpassword.EmailExistResult
import com.minhtu.firesocialmedia.domain.error.signin.SignInError
import com.minhtu.firesocialmedia.data.remote.service.auth.AuthService
import com.minhtu.firesocialmedia.platform.logMessage
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.tasks.await
import kotlin.coroutines.cancellation.CancellationException

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
                .await() // suspend until complete
            Result.success(Unit)
        } catch (ex: Exception) {
            Result.failure(SignInError.Unknown(ex.message ?: "Unknown error"))
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
                        .addOnCompleteListener(context as Activity) { task ->
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
}