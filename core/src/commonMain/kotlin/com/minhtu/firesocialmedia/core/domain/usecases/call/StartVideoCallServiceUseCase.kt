package com.minhtu.firesocialmedia.core.domain.usecases.call

import com.minhtu.firesocialmedia.core.domain.entity.call.OfferAnswer
import com.minhtu.firesocialmedia.core.domain.entity.user.UserInstance
import com.minhtu.firesocialmedia.core.domain.repository.CallRepository

class StartVideoCallServiceUseCase(
    private val callRepository: CallRepository
) {
    suspend operator fun invoke(
        sessionId : String,
        caller : UserInstance,
        callee : UserInstance,
        currentUserId : String?,
        remoteVideoOffer : OfferAnswer?) {
        callRepository.startVideoCallService(
            sessionId,
            caller,
            callee,
            currentUserId,
            remoteVideoOffer
        )
    }
}