package com.minhtu.firesocialmedia.domain.usecases.signin

import com.minhtu.firesocialmedia.domain.repository.AuthenticationRepository

class RememberPasswordUseCase(
    private val authenticationRepository: AuthenticationRepository
) {
    suspend operator fun invoke(email: String,
                                password: String){
        authenticationRepository.saveAccountToLocalStorage(email, password)
    }
}