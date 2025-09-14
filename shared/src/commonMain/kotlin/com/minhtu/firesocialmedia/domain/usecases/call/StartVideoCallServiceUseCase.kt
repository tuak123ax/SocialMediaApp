package com.minhtu.firesocialmedia.domain.usecases.call

import com.minhtu.firesocialmedia.domain.entity.call.OfferAnswer
import com.minhtu.firesocialmedia.domain.entity.user.UserInstance
import com.minhtu.firesocialmedia.domain.repository.CallRepository

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