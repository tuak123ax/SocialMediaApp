package com.minhtu.firesocialmedia.core.domain.usecases.information

import com.minhtu.firesocialmedia.core.domain.repository.CallRepository

class CheckCalleeAvailableUseCase(
    private val callRepository: CallRepository
){
    suspend operator fun invoke(calleeId: String) : Boolean? {
        return callRepository.isCalleeInActiveCall(
            calleeId
        )
    }
}