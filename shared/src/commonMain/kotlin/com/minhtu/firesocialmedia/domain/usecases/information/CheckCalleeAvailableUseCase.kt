package com.minhtu.firesocialmedia.domain.usecases.information

import com.minhtu.firesocialmedia.domain.repository.CallRepository

class CheckCalleeAvailableUseCase(
    private val callRepository: CallRepository
){
    suspend operator fun invoke(calleeId: String) : Boolean? {
        return callRepository.isCalleeInActiveCall(
            calleeId
        )
    }
}