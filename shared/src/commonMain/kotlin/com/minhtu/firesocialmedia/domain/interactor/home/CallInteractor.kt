package com.minhtu.firesocialmedia.domain.interactor.home

import com.minhtu.firesocialmedia.domain.entity.call.CallingRequestData
import kotlinx.coroutines.flow.MutableStateFlow

interface CallInteractor {
    suspend fun observe(
        isInCall: MutableStateFlow<Boolean>,
        userId: String,
        onReceivePhoneCallRequest: suspend (CallingRequestData) -> Unit,
        onEndCall: suspend () -> Unit,
        whoEndCallCallBack : suspend (String) -> Unit
    )

    fun stopObservePhoneCall()

    suspend fun stopCallService()
}