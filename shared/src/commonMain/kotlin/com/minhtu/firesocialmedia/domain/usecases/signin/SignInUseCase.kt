package com.minhtu.firesocialmedia.domain.usecases.signin

import com.minhtu.firesocialmedia.domain.repository.AuthenticationRepository

class SignInUseCase(
    private val authenticationRepository: AuthenticationRepository
) {
    suspend operator fun invoke(email: String,
                                password: String) : Result<Unit>{
        return authenticationRepository.signInWithEmailAndPassword(email, password)
    }
}