package com.minhtu.firesocialmedia.core.domain.usecases.forgotpassword

import com.minhtu.firesocialmedia.core.domain.entity.forgotpassword.EmailExistResult
import com.minhtu.firesocialmedia.core.domain.repository.AuthenticationRepository

class CheckIfEmailExistsUseCase(
    private val authenticationRepository: AuthenticationRepository
) {
    suspend operator fun invoke(email : String) : EmailExistResult {
        return authenticationRepository.fetchSignInMethodsForEmail(email)
    }
}