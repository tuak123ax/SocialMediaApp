package com.minhtu.firesocialmedia.android.service.serviceimpl.auth

import com.google.android.gms.auth.api.identity.SignInCredential
import com.google.firebase.Firebase
import com.google.firebase.FirebaseException
import com.google.firebase.FirebaseNetworkException
import com.google.firebase.FirebaseTooManyRequestsException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthException
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.auth.FirebaseAuthMultiFactorException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseAuthWeakPasswordException
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.auth
import com.minhtu.firesocialmedia.data.remote.service.auth.auth.AuthException
import com.minhtu.firesocialmedia.data.remote.service.auth.AuthService
import com.minhtu.firesocialmedia.platform.logMessage
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.tasks.await
import kotlin.coroutines.cancellation.CancellationException
import kotlin.coroutines.resume

class AndroidAuthService : AuthService {
    override suspend fun signInWithEmailAndPassword(
        email: String,
        password: String
    ): Result<Unit> {
        return try {
            FirebaseAuth.getInstance()
                .signInWithEmailAndPassword(email, password)
                .await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            Result.failure(AuthException("UNKNOWN", e.message ?: "Unknown error"))
        } catch (e: FirebaseAuthInvalidCredentialsException) {
            val code = when (e.errorCode) {
                "ERROR_INVALID_EMAIL" -> "ERROR_INVALID_EMAIL"
                "ERROR_WRONG_PASSWORD" -> "ERROR_WRONG_PASSWORD"
                else -> "INVALID_CREDENTIALS"
            }
            Result.failure(AuthException(code, e.message))
        } catch (e: FirebaseAuthInvalidUserException) {
            val code = when (e.errorCode) {
                "ERROR_USER_DISABLED" -> "ERROR_USER_DISABLED"
                "ERROR_USER_NOT_FOUND" -> "ERROR_USER_NOT_FOUND"
                else -> "INVALID_USER"
            }
            Result.failure(AuthException(code, e.message))
        } catch (e: FirebaseTooManyRequestsException) {
            Result.failure(AuthException("TOO_MANY_REQUESTS", e.message))
        } catch (e: FirebaseNetworkException) {
            Result.failure(AuthException("NETWORK_ERROR", e.message))
        } catch (e: FirebaseAuthMultiFactorException) {
            Result.failure(AuthException("MULTI_FACTOR", e.message))
        } catch (e: FirebaseException) {
            Result.failure(AuthException("UNKNOWN", e.message ?: "Unknown error"))
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
            Result.failure(AuthException("WEAK_PASSWORD", e.message))
        } catch (e: FirebaseAuthInvalidCredentialsException) {
            Result.failure(AuthException("ERROR_INVALID_EMAIL", e.message))
        } catch (e: FirebaseAuthUserCollisionException) {
            Result.failure(AuthException("EMAIL_ALREADY_IN_USE", e.message))
        } catch (e: FirebaseNetworkException) {
            Result.failure(AuthException("NETWORK_ERROR", e.message))
        } catch (e: FirebaseAuthException) {
            Result.failure(AuthException("UNKNOWN", e.message ?: "Authentication error"))
        } catch (e: Exception) {
            Result.failure(AuthException("UNKNOWN", e.message ?: "Unknown error"))
        }
    }

    override suspend fun fetchSignInMethodsForEmail(email: String): Boolean =
        suspendCancellableCoroutine { continuation ->
            FirebaseAuth.getInstance().fetchSignInMethodsForEmail(email)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        val signInMethods = task.result?.signInMethods
                        if (continuation.isActive) continuation.resume(!signInMethods.isNullOrEmpty())
                    } else {
                        if (continuation.isActive) continuation.resume(false)
                    }
                }
        }

    override suspend fun sendPasswordResetEmail(email: String): Boolean =
        suspendCancellableCoroutine { continuation ->
            FirebaseAuth.getInstance().sendPasswordResetEmail(email)
                .addOnCompleteListener { task ->
                    if (continuation.isActive) continuation.resume(true)
                }
        }

    override suspend fun handleSignInGoogleResult(credential: Any): String? =
        suspendCancellableCoroutine { continuation ->
            val idToken = (credential as SignInCredential).googleIdToken
            when {
                idToken != null -> {
                    val firebaseCredential = GoogleAuthProvider.getCredential(idToken, null)
                    Firebase.auth.signInWithCredential(firebaseCredential)
                        .addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                val user = Firebase.auth.currentUser
                                if (user != null && continuation.isActive) {
                                    continuation.resume(user.email)
                                }
                            } else {
                                continuation.resume(null)
                            }
                        }
                }
                else -> {
                    logMessage("Signin", { "No ID token!" })
                    continuation.resume(null)
                }
            }
        }
}
