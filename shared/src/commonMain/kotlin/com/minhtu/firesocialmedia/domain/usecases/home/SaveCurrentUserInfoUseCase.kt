package com.minhtu.firesocialmedia.domain.usecases.home

import com.minhtu.firesocialmedia.domain.entity.user.UserInstance
import com.minhtu.firesocialmedia.domain.repository.LocalRepository

class SaveCurrentUserInfoUseCase(
    private val localRepository: LocalRepository
) {
    suspend operator fun invoke(user : UserInstance) {
        localRepository.saveCurrentUserInfo(user)
    }
}