package com.minhtu.firesocialmedia.core.domain.usecases.common

import com.minhtu.firesocialmedia.core.domain.repository.LocalRepository

class GetFCMTokenUseCase(
    private val localRepository: LocalRepository
) {
    suspend operator fun invoke() : String{
        return localRepository.getFCMToken()
    }
}