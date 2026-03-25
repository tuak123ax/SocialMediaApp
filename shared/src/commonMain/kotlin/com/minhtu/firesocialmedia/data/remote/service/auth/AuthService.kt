package com.minhtu.firesocialmedia.data.remote.service.auth

import com.minhtu.firesocialmedia.data.remote.dto.user.UserDTO
import com.minhtu.firesocialmedia.domain.entity.forgotpassword.EmailExistResult
import com.minhtu.firesocialmedia.domain.entity.settings.ChangePasswordState
import com.minhtu.firesocialmedia.domain.error.signin.SignInError
import io.mockative.Mockable

@Mockable
interface AuthService {
    suspend fun signInWithEmailAndPassword(email: String, password: String): SignInError?
    suspend fun signUpWithEmailAndPassword(email: String, password: String) : Result<Unit>
    suspend fun getCurrentUserUid() : String?
    suspend fun getCurrentUserEmail() : String?
    suspend fun fetchSignInMethodsForEmail(email: String) : EmailExistResult
    suspend fun sendPasswordResetEmail(email: String) : Boolean
    suspend fun handleSignInGoogleResult(credentialsDTO: Any) : String?
    suspend fun reAuthenticate(currentUserEmail: String,
                               currentPassword: String) : Boolean

    suspend fun changePassword(userDTO : UserDTO,
                               newPassword: String,
                               userPath : String,
                               lastTimeChangePasswordPath : String) : ChangePasswordState
}