package com.minhtu.firesocialmedia.domain.usecases.forgotpassword

import com.minhtu.firesocialmedia.domain.entity.forgotpassword.EmailExistResult
import com.minhtu.firesocialmedia.domain.repository.AuthenticationRepository

class CheckIfEmailExistsUseCase(
    private val authenticationRepository: AuthenticationRepository
) {
    suspend operator fun invoke(email : String) : EmailExistResult {
        return authenticationRepository.fetchSignInMethodsForEmail(email)
    }
}