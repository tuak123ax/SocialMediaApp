package com.minhtu.firesocialmedia.core.domain.usecases.signin

import com.minhtu.firesocialmedia.core.domain.error.signin.SignInError
import com.minhtu.firesocialmedia.core.domain.repository.AuthenticationRepository

class SignInUseCase(
    private val authenticationRepository: AuthenticationRepository
) {
    suspend operator fun invoke(email: String,
                                password: String) : SignInError?{
        return authenticationRepository.signInWithEmailAndPassword(email, password)
    }
}