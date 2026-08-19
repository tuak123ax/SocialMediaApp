package com.minhtu.firesocialmedia.domain.usecases.call

import com.minhtu.firesocialmedia.domain.repository.CallRepository

class UpdateCameraStatusUseCase(
    private val callRepository: CallRepository
) {
    suspend operator fun invoke(cameraOff: Boolean) {
        callRepository.updateCameraStatus(cameraOff)
    }
}
