package com.minhtu.firesocialmedia.domain.usecases.information

import com.minhtu.firesocialmedia.domain.entity.user.UserInstance
import com.minhtu.firesocialmedia.domain.repository.AuthenticationRepository

class SaveSignUpInformationUseCase(
    private val authenticationRepository: AuthenticationRepository
) {
    suspend operator fun invoke(userInstance: UserInstance) : Boolean{
        return authenticationRepository.saveSignUpInformation(userInstance)
    }
}