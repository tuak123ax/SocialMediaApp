package com.minhtu.firesocialmedia.core.domain.usecases.signup

import com.minhtu.firesocialmedia.core.domain.repository.AuthenticationRepository

class SignUpUseCase(
    private val authenticationRepository: AuthenticationRepository
) {
    suspend operator fun invoke(
        email : String,
        password : String
    ) : Result<Unit>{
        return authenticationRepository.signUpWithEmailAndPassword(email, password)
    }
}