package com.minhtu.firesocialmedia.application.interactor

import com.minhtu.firesocialmedia.domain.entity.call.OfferAnswer
import com.minhtu.firesocialmedia.domain.interactor.home.CallInteractor
import com.minhtu.firesocialmedia.domain.usecases.call.ObservePhoneCallWithInCallUseCase
import kotlinx.coroutines.flow.MutableStateFlow

class CallInteractorImpl(
    private val observePhoneCallUseCase : ObservePhoneCallWithInCallUseCase
) : CallInteractor {
    override suspend fun observe(
        isInCall: MutableStateFlow<Boolean>,
        userId: String,
        onReceivePhoneCallRequest: suspend (String, String, String, OfferAnswer) -> Unit,
        onEndCall: () -> Unit
    ) {
        observePhoneCallUseCase.invoke(
            isInCall, userId,
            onReceivePhoneCallRequest = { remoteSessionId, remoteOffer, remoteCallerId, remoteCalleeId ->
                onReceivePhoneCallRequest(remoteSessionId, remoteCallerId, remoteCalleeId,remoteOffer)
            },
            onEndCall = {
                onEndCall()
            }
        )
    }
}