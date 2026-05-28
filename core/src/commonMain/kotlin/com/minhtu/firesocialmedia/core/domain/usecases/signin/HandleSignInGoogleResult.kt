package com.minhtu.firesocialmedia.core.domain.usecases.signin

import com.minhtu.firesocialmedia.core.domain.repository.AuthenticationRepository

class HandleSignInGoogleResultUseCase(
    private val authenticationRepository: AuthenticationRepository
) {
    suspend operator fun invoke(credentials: Any) : String?{
        return authenticationRepository.handleSignInGoogleResult(credentials)
    }
}