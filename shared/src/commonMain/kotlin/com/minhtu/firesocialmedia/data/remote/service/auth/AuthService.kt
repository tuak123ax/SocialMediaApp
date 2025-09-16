package com.minhtu.firesocialmedia.data.remote.service.auth

import com.minhtu.firesocialmedia.domain.entity.forgotpassword.EmailExistResult
import io.mockative.Mockable

@Mockable
interface AuthService {
    suspend fun signInWithEmailAndPassword(email: String, password: String): Result<Unit>
    suspend fun signUpWithEmailAndPassword(email: String, password: String) : Result<Unit>
    suspend fun getCurrentUserUid() : String?
    suspend fun getCurrentUserEmail() : String?
    suspend fun fetchSignInMethodsForEmail(email: String) : EmailExistResult
    suspend fun sendPasswordResetEmail(email: String) : Boolean
    suspend fun handleSignInGoogleResult(credentialsDTO: Any) : String?
}