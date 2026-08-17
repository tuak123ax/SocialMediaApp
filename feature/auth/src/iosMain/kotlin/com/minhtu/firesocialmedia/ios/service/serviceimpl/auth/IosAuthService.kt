package com.minhtu.firesocialmedia.ios.service.serviceimpl.auth

import cocoapods.FirebaseAuth.FIRAuth
import com.minhtu.firesocialmedia.data.remote.service.auth.auth.AuthException
import com.minhtu.firesocialmedia.data.remote.service.auth.AuthService
import com.minhtu.firesocialmedia.platform.logMessage
import kotlinx.coroutines.suspendCancellableCoroutine

@Suppress("UNCHECKED_CAST")
class IosAuthService : AuthService {
    override suspend fun signInWithEmailAndPassword(
        email: String,
        password: String
    ): Result<Unit> {
        return suspendCancellableCoroutine { cont ->
            FIRAuth.auth().signInWithEmail(email, password = password) { authResult, error ->
                if (error != null) {
                    logMessage("iOSAuth") { "signIn error: ${error.localizedDescription}" }
                    cont.resume(Result.failure(AuthException("UNKNOWN", error.localizedDescription))) {}
                } else {
                    logMessage("iOSAuth") { "signIn success" }
                    cont.resume(Result.success(Unit)) {}
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
                    cont.resume(Result.failure(AuthException("UNKNOWN", error.localizedDescription ?: "Unknown error")), onCancellation = {})
                } else {
                    cont.resume(Result.success(Unit), onCancellation = {})
                }
            }
        }
    }

    override suspend fun fetchSignInMethodsForEmail(email: String): Boolean = suspendCancellableCoroutine { continuation ->
        FIRAuth.auth().fetchSignInMethodsForEmail(email) { result, error ->
            if (error != null || result == null) {
                if (continuation.isActive) continuation.resume(false) {}
            } else {
                if (continuation.isActive) continuation.resume(result.isNotEmpty()) {}
            }
        }
    }

    override suspend fun sendPasswordResetEmail(email: String): Boolean = suspendCancellableCoroutine { continuation ->
        FIRAuth.auth().sendPasswordResetWithEmail(email) { error ->
            if (continuation.isActive) continuation.resume(error == null) {}
        }
    }

    override suspend fun handleSignInGoogleResult(credential: Any): String? {
        //Not yet implemented
        return ""
    }
}
