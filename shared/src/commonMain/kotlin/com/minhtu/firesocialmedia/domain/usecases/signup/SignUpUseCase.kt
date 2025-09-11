package com.minhtu.firesocialmedia.domain.usecases.signup

import com.minhtu.firesocialmedia.domain.repository.AuthenticationRepository

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