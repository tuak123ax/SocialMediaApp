package com.minhtu.firesocialmedia.core.domain.usecases.call

import com.minhtu.firesocialmedia.core.domain.entity.user.UserInstance
import com.minhtu.firesocialmedia.core.domain.repository.CallRepository

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