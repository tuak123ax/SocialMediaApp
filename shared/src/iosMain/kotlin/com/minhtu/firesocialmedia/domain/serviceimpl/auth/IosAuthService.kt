package com.minhtu.firesocialmedia.domain.serviceimpl.auth

import cocoapods.FirebaseAuth.FIRAuth
import com.minhtu.firesocialmedia.constants.Constants
import com.minhtu.firesocialmedia.domain.serviceimpl.AuthService
import com.minhtu.firesocialmedia.data.model.signin.SignInError
import com.minhtu.firesocialmedia.utils.Utils
import kotlinx.coroutines.suspendCancellableCoroutine

class IosAuthService() : AuthService{
    override suspend fun signInWithEmailAndPassword(
        email: String,
        password: String
    ): Result<Unit> {
        return suspendCancellableCoroutine { cont ->
            FIRAuth.auth().signInWithEmail(email, password = password) { authResult, error ->
                if (error != null) {
                    com.minhtu.firesocialmedia.platform.logMessage("iOSAuth") { "signIn error: ${error.localizedDescription}" }
                    cont.resume(Result.failure(SignInError.Unknown(error.localizedDescription)), onCancellation = {})
                } else {
                    com.minhtu.firesocialmedia.platform.logMessage("iOSAuth") { "signIn success" }
                    cont.resume(Result.success(Unit), onCancellation = {})
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

    override fun getCurrentUserUid(): String? {
        return FIRAuth.auth().currentUser()?.uid()
    }

    override fun getCurrentUserEmail(): String? {
        return FIRAuth.auth().currentUser()?.email()
    }

    override fun fetchSignInMethodsForEmail(
        email: String,
        callback : Utils.Companion.FetchSignInMethodCallback
    ) {
        FIRAuth.auth().fetchSignInMethodsForEmail(email) { result, error ->
            if (error != null || result == null) {
                com.minhtu.firesocialmedia.platform.logMessage("iOSAuth") { "fetchSignInMethods error: ${error?.localizedDescription ?: "nil"}" }
                callback.onFailure(Pair(false, Constants.EMAIL_SERVER_ERROR))
            } else {
                com.minhtu.firesocialmedia.platform.logMessage("iOSAuth") { "fetchSignInMethods: ${result.joinToString()}" }
                if (result.isNotEmpty()) {
                    callback.onSuccess(Pair(true, Constants.EMAIL_EXISTED))
                } else {
                    callback.onFailure(Pair(false, Constants.EMAIL_NOT_EXISTED))
                }
            }
        }
    }

    override fun sendPasswordResetEmail(
        email: String,
        callback: Utils.Companion.SendPasswordResetEmailCallback
    ) {
        FIRAuth.auth().sendPasswordResetWithEmail(email) { error ->
            if(error == null) {
                callback.onSuccess()
            } else {
                callback.onFailure()
            }
        }
    }

    override fun handleSignInGoogleResult(
        credentials: Any,
        callback: Utils.Companion.SignInGoogleCallback
    ) {

    }
}