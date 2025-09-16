package com.minhtu.firesocialmedia.domain.usecases.signin

import com.minhtu.firesocialmedia.domain.entity.crypto.Credentials
import com.minhtu.firesocialmedia.domain.repository.AuthenticationRepository

class CheckLocalAccountUseCase(
    private val authenticationRepository: AuthenticationRepository
) {
    suspend operator fun invoke() :  Credentials?{
        return authenticationRepository.checkLocalAccount()
    }
}