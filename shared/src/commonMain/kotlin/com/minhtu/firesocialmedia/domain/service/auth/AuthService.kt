package com.minhtu.firesocialmedia.domain.serviceimpl

import com.minhtu.firesocialmedia.data.dto.forgotpassword.EmailExistDTO
import com.minhtu.firesocialmedia.utils.Utils
import io.mockative.Mockable

@Mockable
interface AuthService {
    suspend fun signInWithEmailAndPassword(email: String, password: String): Result<Unit>
    suspend fun signUpWithEmailAndPassword(email: String, password: String) : Result<Unit>
    suspend fun getCurrentUserUid() : String?
    suspend fun getCurrentUserEmail() : String?
    suspend fun fetchSignInMethodsForEmail(email: String) : EmailExistDTO
    suspend fun sendPasswordResetEmail(email: String) : Boolean
    suspend fun handleSignInGoogleResult(credentials: Any) : String?
}