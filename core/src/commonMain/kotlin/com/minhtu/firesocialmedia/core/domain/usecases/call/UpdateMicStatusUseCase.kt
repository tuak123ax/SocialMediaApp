package com.minhtu.firesocialmedia.core.domain.usecases.call

import com.minhtu.firesocialmedia.core.domain.repository.CallRepository

class UpdateMicStatusUseCase(
    private val callRepository: CallRepository
) {
    suspend operator fun invoke(micMuted: Boolean) {
        callRepository.updateMuteStatus(micMuted)
    }
}