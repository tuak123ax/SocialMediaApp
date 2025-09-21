package com.minhtu.firesocialmedia.application.interactor

import com.minhtu.firesocialmedia.domain.entity.call.CallingRequestData
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
        onReceivePhoneCallRequest: suspend (CallingRequestData) -> Unit,
        onEndCall: () -> Unit
    ) {
        observePhoneCallUseCase.invoke(
            isInCall, userId,
            onReceivePhoneCallRequest = { callingRequestData ->
                onReceivePhoneCallRequest(callingRequestData)
            },
            onEndCall = {
                onEndCall()
            }
        )
    }
}