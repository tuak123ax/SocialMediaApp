package com.minhtu.firesocialmedia.domain.usecases.call

import com.minhtu.firesocialmedia.domain.entity.user.UserInstance
import com.minhtu.firesocialmedia.domain.repository.CallRepository

class StartCallServiceUseCase(
    private val callRepository: CallRepository
) {
    suspend operator fun invoke(
        sessionId : String,
        caller : UserInstance,
        callee : UserInstance) {
        callRepository.startCallService(sessionId, caller, callee)
    }
}