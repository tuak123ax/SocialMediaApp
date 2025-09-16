package com.minhtu.firesocialmedia.domain.usecases.common

import com.minhtu.firesocialmedia.domain.repository.LocalRepository

class GetFCMTokenUseCase(
    private val localRepository: LocalRepository
) {
    suspend operator fun invoke() : String{
        return localRepository.getFCMToken()
    }
}