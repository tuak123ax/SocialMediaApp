package com.minhtu.firesocialmedia.core.domain.usecases.call

import com.minhtu.firesocialmedia.core.domain.entity.call.SpeakerType
import com.minhtu.firesocialmedia.core.domain.repository.CallRepository

class UpdateSpeakerStatusUseCase(
    private val callRepository: CallRepository
) {
    suspend operator fun invoke(speakerType: SpeakerType) {
        callRepository.updateSpeakerStatus(speakerType)
    }
}