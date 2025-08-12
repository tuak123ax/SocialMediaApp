package com.minhtu.firesocialmedia.domain.serviceimpl

import com.minhtu.firesocialmedia.utils.Utils
import io.mockative.Mockable

@Mockable
interface AuthService {
    suspend fun signInWithEmailAndPassword(email: String, password: String): Result<Unit>
    suspend fun signUpWithEmailAndPassword(email: String, password: String) : Result<Unit>
    fun getCurrentUserUid() : String?
    fun getCurrentUserEmail() : String?
    fun fetchSignInMethodsForEmail(email: String, callback: Utils.Companion.FetchSignInMethodCallback)
    fun sendPasswordResetEmail(email: String, callback: Utils.Companion.SendPasswordResetEmailCallback)
    fun handleSignInGoogleResult(
        credentials: Any,
        callback: Utils.Companion.SignInGoogleCallback
    )
}