package com.minhtu.firesocialmedia.core.domain.repository

import com.minhtu.firesocialmedia.core.domain.entity.crypto.Credentials
import com.minhtu.firesocialmedia.core.domain.entity.forgotpassword.EmailExistResult
import com.minhtu.firesocialmedia.core.domain.entity.signin.SignInState
import com.minhtu.firesocialmedia.core.domain.entity.user.UserInstance
import com.minhtu.firesocialmedia.core.domain.error.signin.SignInError

interface AuthenticationRepository {
    suspend fun signInWithEmailAndPassword(email: String,
                                   password: String) : SignInError?
    suspend fun saveAccountToLocalStorage(email: String,
                                          password: String)
    suspend fun checkUserExists(email : String) : SignInState
    suspend fun checkLocalAccount() : Credentials?
    suspend fun handleSignInGoogleResult(credential: Any): String?
    suspend fun signUpWithEmailAndPassword(email: String, password: String) : Result<Unit>
    suspend fun fetchSignInMethodsForEmail(email : String) : EmailExistResult
    suspend fun sendPasswordResetEmail(email: String) : Boolean
    suspend fun clearAccount()
    suspend fun saveSignUpInformation(userInstance: UserInstance) : Boolean
}