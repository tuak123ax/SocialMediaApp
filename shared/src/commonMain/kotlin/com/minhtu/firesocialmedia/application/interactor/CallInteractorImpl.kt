package com.minhtu.firesocialmedia.application.interactor

import com.minhtu.firesocialmedia.domain.entity.call.CallingRequestData
import com.minhtu.firesocialmedia.domain.entity.call.OfferAnswer
import com.minhtu.firesocialmedia.domain.interactor.home.CallInteractor
import com.minhtu.firesocialmedia.domain.usecases.call.ObservePhoneCallWithInCallUseCase
import com.minhtu.firesocialmedia.domain.usecases.call.StopCallServiceUseCase
import com.minhtu.firesocialmedia.domain.usecases.call.StopObservePhoneCallUseCase
import kotlinx.coroutines.flow.MutableStateFlow

class CallInteractorImpl(
    private val observePhoneCallUseCase : ObservePhoneCallWithInCallUseCase,
    private val stopObservePhoneCallUseCase : StopObservePhoneCallUseCase,
    private val stopCallServiceUseCase : StopCallServiceUseCase
) : CallInteractor {
    override suspend fun observe(
        isInCall: MutableStateFlow<Boolean>,
        userId: String,
        onReceivePhoneCallRequest: suspend (CallingRequestData) -> Unit,
        onEndCall: suspend () -> Unit,
        whoEndCallCallBack : suspend (String) -> Unit
    ) {
        observePhoneCallUseCase.invoke(
            isInCall, userId,
            onReceivePhoneCallRequest = { callingRequestData ->
                onReceivePhoneCallRequest(callingRequestData)
            },
            whoEndCallCallBack = { whoEndCall ->
                whoEndCallCallBack(whoEndCall)
            },
            onEndCall = {
                onEndCall()
            }
        )
    }

    override fun stopObservePhoneCall() {
        stopObservePhoneCallUseCase.invoke()
    }

    override suspend fun stopCallService() {
        stopCallServiceUseCase.invoke()
    }
}