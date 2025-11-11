package com.minhtu.firesocialmedia.domain.usecases.newsfeed

import com.minhtu.firesocialmedia.domain.repository.CallRepository

class RequestCameraPermissionUseCase(
    private val callRepository: CallRepository
) {
    suspend operator fun invoke() : Boolean {
        return callRepository.requestCameraPermission()
    }
}