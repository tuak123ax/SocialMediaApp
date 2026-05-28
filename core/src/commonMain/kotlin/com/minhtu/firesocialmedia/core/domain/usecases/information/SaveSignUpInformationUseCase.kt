package com.minhtu.firesocialmedia.core.domain.usecases.information

import com.minhtu.firesocialmedia.core.domain.entity.user.UserInstance
import com.minhtu.firesocialmedia.core.domain.repository.AuthenticationRepository

class SaveSignUpInformationUseCase(
    private val authenticationRepository: AuthenticationRepository
) {
    suspend operator fun invoke(userInstance: UserInstance) : Boolean{
        return authenticationRepository.saveSignUpInformation(userInstance)
    }
}