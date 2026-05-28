package com.minhtu.firesocialmedia.core.domain.usecases.signin

import com.minhtu.firesocialmedia.core.domain.repository.CommonDbRepository

class SaveLoginActivityInfoUseCase(
    private val commonDbRepository: CommonDbRepository
) {
    suspend operator fun invoke(userId : String) {
        commonDbRepository.saveLoginActivityInfo(userId)
    }
}