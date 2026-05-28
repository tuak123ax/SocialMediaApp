package com.minhtu.firesocialmedia.core.domain.usecases.call

import com.minhtu.firesocialmedia.core.domain.repository.CallRepository

class UpdateCameraStatusUseCase(
    private val callRepository: CallRepository
) {
    suspend operator fun invoke(cameraOff: Boolean) {
        callRepository.updateCameraStatus(cameraOff)
    }
}
