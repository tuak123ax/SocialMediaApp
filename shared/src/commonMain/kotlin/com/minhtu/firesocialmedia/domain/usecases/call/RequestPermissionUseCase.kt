package com.minhtu.firesocialmedia.domain.usecases.call

import com.minhtu.firesocialmedia.domain.repository.CallRepository

class RequestPermissionUseCase(
    private val callRepository: CallRepository
) {
    suspend operator fun invoke() : Boolean {
        return callRepository.requestAudioPermission()
    }
}