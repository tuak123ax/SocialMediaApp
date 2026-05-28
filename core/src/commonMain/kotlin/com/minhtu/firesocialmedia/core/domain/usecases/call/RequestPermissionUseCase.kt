package com.minhtu.firesocialmedia.core.domain.usecases.call

import com.minhtu.firesocialmedia.core.domain.repository.CallRepository

class RequestPermissionUseCase(
    private val callRepository: CallRepository
) {
    suspend operator fun invoke() : Boolean {
        return callRepository.requestAudioPermission()
    }
}