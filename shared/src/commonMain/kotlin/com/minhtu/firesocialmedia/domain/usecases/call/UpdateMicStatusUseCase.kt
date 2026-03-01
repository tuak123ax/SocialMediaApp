package com.minhtu.firesocialmedia.domain.usecases.call

import com.minhtu.firesocialmedia.domain.repository.CallRepository

class UpdateMicStatusUseCase(
    private val callRepository: CallRepository
) {
    suspend operator fun invoke(micMuted: Boolean) {
        callRepository.updateMuteStatus(micMuted)
    }
}