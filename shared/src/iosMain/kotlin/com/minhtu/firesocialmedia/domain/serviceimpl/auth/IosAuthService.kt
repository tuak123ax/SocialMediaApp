package com.minhtu.firesocialmedia.domain.serviceimpl.auth

import cocoapods.FirebaseAuth.FIRAuth
import com.minhtu.firesocialmedia.constants.Constants
import com.minhtu.firesocialmedia.domain.entity.forgotpassword.EmailExistResult
import com.minhtu.firesocialmedia.domain.error.signin.SignInError
import com.minhtu.firesocialmedia.data.remote.service.auth.AuthService
import kotlinx.coroutines.suspendCancellableCoroutine

class IosAuthService() : AuthService{
    override suspend fun signInWithEmailAndPassword(
        email: String,
        password: String
    ): SignInError? {
        return suspendCancellableCoroutine { cont ->
            FIRAuth.auth().signInWithEmail(email, password = password) { authResult, error ->
                if (error != null) {
                    com.minhtu.firesocialmedia.platform.logMessage("iOSAuth") { "signIn error: ${error.localizedDescription}" }
                    cont.resume(SignInError.Unknown(error.localizedDescription), onCancellation = {})
                } else {
                    com.minhtu.firesocialmedia.platform.logMessage("iOSAuth") { "signIn success" }
                    cont.resume(null, onCancellation = {})
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
                if(continuation.isActive) continuation.resume(EmailExistResult(false, Constants.EMAIL_SERVER_ERROR), onCancellation = {})
            } else {
                if (result.isNotEmpty()) {
                    if(continuation.isActive) continuation.resume(EmailExistResult(true, Constants.EMAIL_EXISTED), onCancellation = {})
                } else {
                    if(continuation.isActive) continuation.resume(EmailExistResult(false, Constants.EMAIL_NOT_EXISTED), onCancellation = {})
                }
            }
        }
    }

    override suspend fun sendPasswordResetEmail(email: String): Boolean = suspendCancellableCoroutine{ continuation ->
        FIRAuth.auth().sendPasswordResetWithEmail(email) { error ->
            if(error == null) {
                if(continuation.isActive) continuation.resume(true, onCancellation = {})
            } else {
                if(continuation.isActive) continuation.resume(false, onCancellation = {})
            }
        }
    }

    override suspend fun handleSignInGoogleResult(credentials: Any): String? {
        //Not yet implemented
        return ""
    }
}