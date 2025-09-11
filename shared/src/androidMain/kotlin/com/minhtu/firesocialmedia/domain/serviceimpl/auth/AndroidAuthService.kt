package com.minhtu.firesocialmedia.domain.serviceimpl.auth

import android.app.Activity
import android.content.Context
import com.google.android.gms.auth.api.identity.SignInCredential
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.auth
import com.minhtu.firesocialmedia.constants.Constants
import com.minhtu.firesocialmedia.data.dto.forgotpassword.EmailExistDTO
import com.minhtu.firesocialmedia.domain.error.signin.SignInError
import com.minhtu.firesocialmedia.domain.serviceimpl.AuthService
import com.minhtu.firesocialmedia.platform.logMessage
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.tasks.await

class AndroidAuthService(var context: Context) : AuthService{
    override suspend fun signInWithEmailAndPassword(
        email: String,
        password: String
    ): Result<Unit> {
        return try {
            FirebaseAuth.getInstance()
                .signInWithEmailAndPassword(email, password)
                .await() // suspend until complete
            Result.success(Unit)
        } catch (ex: Exception) {
            // You can parse exception here to map to your custom error
            Result.failure(SignInError.Unknown(ex.message ?: "Unknown error"))
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
            // You can parse exception here to map to your custom error
            Result.failure(SignInError.Unknown(ex.message ?: "Unknown error"))
        }
    }

    override suspend fun getCurrentUserUid(): String? = suspendCancellableCoroutine{ continuation ->
        if(continuation.isActive) continuation.resume(FirebaseAuth.getInstance().uid, onCancellation = {})
    }

    override suspend fun getCurrentUserEmail(): String? = suspendCancellableCoroutine{ continuation ->
        if(continuation.isActive) continuation.resume(FirebaseAuth.getInstance().currentUser?.email.toString(), onCancellation = {})
    }

    override suspend fun fetchSignInMethodsForEmail(email: String) : EmailExistDTO = suspendCancellableCoroutine{ continuation ->
        FirebaseAuth.getInstance().fetchSignInMethodsForEmail(email)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val signInMethods = task.result?.signInMethods
                    if (signInMethods.isNullOrEmpty()) {
                        if(continuation.isActive)
                            continuation.resume(EmailExistDTO(false, Constants.EMAIL_NOT_EXISTED),
                                onCancellation = {})
                    } else {
                        continuation.resume(EmailExistDTO(true, Constants.EMAIL_EXISTED),
                            onCancellation = {})
                    }
                } else {
                    continuation.resume(EmailExistDTO(false, Constants.EMAIL_SERVER_ERROR),
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