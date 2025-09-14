package com.minhtu.firesocialmedia.domain.interactor.home

import com.minhtu.firesocialmedia.domain.entity.call.OfferAnswer
import kotlinx.coroutines.flow.MutableStateFlow

interface CallInteractor {
    suspend fun observe(
        isInCall: MutableStateFlow<Boolean>,
        userId: String,
        onReceivePhoneCallRequest: suspend (sessionId: String, callerId: String, calleeId: String, offer: OfferAnswer) -> Unit,
        onEndCall: () -> Unit
    )
}