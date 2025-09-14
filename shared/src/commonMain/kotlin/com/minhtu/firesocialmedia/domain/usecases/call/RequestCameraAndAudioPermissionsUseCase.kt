package com.minhtu.firesocialmedia.domain.usecases.call

import com.minhtu.firesocialmedia.domain.repository.CallRepository

class RequestCameraAndAudioPermissionsUseCase(
    private val callRepository: CallRepository) {
        suspend operator fun invoke() : Boolean {
            return callRepository.requestCameraAndAudioPermissions()
        }
    }