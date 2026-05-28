package com.minhtu.firesocialmedia.core.domain.usecases.signin

import com.minhtu.firesocialmedia.core.domain.entity.signin.SignInState
import com.minhtu.firesocialmedia.core.domain.repository.AuthenticationRepository

class CheckUserExistsUseCase(
    private val authenticationRepository: AuthenticationRepository
) {
    suspend operator fun invoke(email: String) : SignInState {
        return authenticationRepository.checkUserExists(email)
    }
}