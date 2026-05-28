package com.minhtu.firesocialmedia.core.domain.usecases.call

import com.minhtu.firesocialmedia.core.domain.repository.CallRepository

class StopCallServiceUseCase(
    private val callRepository: CallRepository
) {
    suspend operator fun invoke() {
        callRepository.stopCallService()
    }
}