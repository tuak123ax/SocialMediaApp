package com.minhtu.firesocialmedia.core.domain.usecases.signin

import com.minhtu.firesocialmedia.core.domain.repository.AuthenticationRepository

class RememberPasswordUseCase(
    private val authenticationRepository: AuthenticationRepository
) {
    suspend operator fun invoke(email: String,
                                password: String){
        authenticationRepository.saveAccountToLocalStorage(email, password)
    }
}