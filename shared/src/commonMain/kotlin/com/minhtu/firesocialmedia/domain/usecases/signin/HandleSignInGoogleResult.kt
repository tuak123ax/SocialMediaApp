package com.minhtu.firesocialmedia.domain.usecases.signin

import com.minhtu.firesocialmedia.domain.repository.AuthenticationRepository

class HandleSignInGoogleResultUseCase(
    private val authenticationRepository: AuthenticationRepository
) {
    suspend operator fun invoke(credentials: Any) : String?{
        return authenticationRepository.handleSignInGoogleResult(credentials)
    }
}