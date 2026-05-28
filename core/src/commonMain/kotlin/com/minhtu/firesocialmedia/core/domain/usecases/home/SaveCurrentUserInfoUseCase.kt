package com.minhtu.firesocialmedia.core.domain.usecases.home

import com.minhtu.firesocialmedia.core.domain.entity.user.UserInstance
import com.minhtu.firesocialmedia.core.domain.repository.LocalRepository

class SaveCurrentUserInfoUseCase(
    private val localRepository: LocalRepository
) {
    suspend operator fun invoke(user : UserInstance) {
        localRepository.saveCurrentUserInfo(user)
    }
}