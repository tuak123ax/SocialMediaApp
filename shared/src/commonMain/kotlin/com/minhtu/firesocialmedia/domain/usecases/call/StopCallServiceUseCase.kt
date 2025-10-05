package com.minhtu.firesocialmedia.domain.usecases.call

import com.minhtu.firesocialmedia.domain.repository.CallRepository

class StopCallServiceUseCase(
    private val callRepository: CallRepository
) {
    suspend operator fun invoke() {
        callRepository.stopCallService()
    }
}