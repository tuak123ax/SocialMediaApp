package com.minhtu.firesocialmedia.domain.usecases.call

import com.minhtu.firesocialmedia.domain.entity.call.SpeakerType
import com.minhtu.firesocialmedia.domain.repository.CallRepository

class UpdateSpeakerStatusUseCase(
    private val callRepository: CallRepository
) {
    suspend operator fun invoke(speakerType: SpeakerType) {
        callRepository.updateSpeakerStatus(speakerType)
    }
}