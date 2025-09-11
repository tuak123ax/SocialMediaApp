package com.minhtu.firesocialmedia.domain.usecases.signin

import com.minhtu.firesocialmedia.domain.entity.signin.SignInState
import com.minhtu.firesocialmedia.domain.repository.AuthenticationRepository

class CheckUserExistsUseCase(
    private val authenticationRepository: AuthenticationRepository
) {
    suspend operator fun invoke(email: String) : SignInState {
        return authenticationRepository.checkUserExists(email)
    }
}