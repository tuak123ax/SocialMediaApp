package com.minhtu.firesocialmedia.domain.usecases.forgotpassword

import com.minhtu.firesocialmedia.domain.repository.AuthenticationRepository

class SendEmailResetPasswordUseCase(
    private val authenticationRepository: AuthenticationRepository
) {
    suspend operator fun invoke(email : String) : Boolean {
        return authenticationRepository.sendPasswordResetEmail(email)
    }
}