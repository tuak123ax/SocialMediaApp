package com.minhtu.firesocialmedia.core.domain.usecases.signin

import com.minhtu.firesocialmedia.core.domain.entity.crypto.Credentials
import com.minhtu.firesocialmedia.core.domain.repository.AuthenticationRepository

class CheckLocalAccountUseCase(
    private val authenticationRepository: AuthenticationRepository
) {
    suspend operator fun invoke() :  Credentials?{
        return authenticationRepository.checkLocalAccount()
    }
}