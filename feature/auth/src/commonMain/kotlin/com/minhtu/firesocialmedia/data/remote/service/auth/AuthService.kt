package com.minhtu.firesocialmedia.data.remote.service.auth

import io.mockative.Mockable

@Mockable
interface AuthService {
    suspend fun signInWithEmailAndPassword(email: String, password: String): Result<Unit>
    suspend fun signUpWithEmailAndPassword(email: String, password: String): Result<Unit>
    suspend fun fetchSignInMethodsForEmail(email: String): Boolean
    suspend fun sendPasswordResetEmail(email: String): Boolean
    suspend fun handleSignInGoogleResult(credential: Any): String?
}
