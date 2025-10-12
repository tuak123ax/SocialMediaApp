package com.minhtu.firesocialmedia.domain.usecases.signin

import com.minhtu.firesocialmedia.domain.error.signin.SignInError
import com.minhtu.firesocialmedia.domain.repository.AuthenticationRepository

class SignInUseCase(
    private val authenticationRepository: AuthenticationRepository
) {
    suspend operator fun invoke(email: String,
                                password: String) : SignInError?{
        return authenticationRepository.signInWithEmailAndPassword(email, password)
    }
}