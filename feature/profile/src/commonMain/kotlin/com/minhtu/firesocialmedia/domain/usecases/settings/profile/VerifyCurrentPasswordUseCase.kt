package com.minhtu.firesocialmedia.domain.usecases.settings.profile

import com.minhtu.firesocialmedia.data.remote.service.auth.ProfileAuthService

class VerifyCurrentPasswordUseCase(
    private val authService: ProfileAuthService
) {
    suspend operator fun invoke(currentUserEmail : String,
                                currentPassword : String) : Boolean {
        return authService.reAuthenticate(currentUserEmail, currentPassword)
    }
}
