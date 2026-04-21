package com.minhtu.firesocialmedia.domain.usecases.signin

import com.minhtu.firesocialmedia.domain.repository.CommonDbRepository

class SaveLoginActivityInfoUseCase(
    private val commonDbRepository: CommonDbRepository
) {
    suspend operator fun invoke(userId : String) {
        commonDbRepository.saveLoginActivityInfo(userId)
    }
}