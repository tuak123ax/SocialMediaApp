package com.minhtu.firesocialmedia.domain.usecases.call

import com.minhtu.firesocialmedia.domain.entity.call.IceCandidateData
import com.minhtu.firesocialmedia.domain.entity.call.OfferAnswer
import com.minhtu.firesocialmedia.utils.Utils
import com.minhtu.firesocialmedia.utils.Utils.Companion.getCallTypeFromSdp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch

data class CalleeUseCases(
    val listenForIncomingCalls : ListenForIncomingCallsUseCase,
    val observePhoneCall : ObservePhoneCallUseCase,
    val sendIceCandidate: SendIceCandidateUseCase,
    val sendAnswer : SendAnswerUseCase,
    val setRemoteDescription : SetRemoteDescriptionUseCase,
    val acceptCall : AcceptCallUseCase,
    val observeVideoCall : ObserveVideoCall,
    val endCallUseCase : EndCallUseCase
)
class ListenForIncomingCallsUseCase(private val initializeCallUseCase: InitializeCallUseCase) {
    suspend operator fun invoke(
        onInitializeFinished : suspend () -> Unit,
        onIceCandidateCreated : suspend (IceCandidateData) -> Unit
    ) {
        //Initialize call service
        initializeCallUseCase.initializeCall(
            onInitializeFinished = {
                onInitializeFinished()
            },
            onIceCandidateCreated = { iceCandidateData ->
                onIceCandidateCreated(iceCandidateData)
            })
    }
}

class ObservePhoneCallUseCase(private val signalingDataUseCase: SendSignalingDataUseCase) {
    suspend operator fun invoke(
        calleeId : String,
        onReceivePhoneCallRequest : suspend (sessionId : String,
                                             offer : OfferAnswer,
                                             callerId : String,
                                             calleeId : String) -> Unit,
        onEndCall : suspend () -> Unit) {
        signalingDataUseCase.observePhoneCallWithoutCheckingInCall(
            calleeId,
            onReceivePhoneCallRequest = { remoteSessionId, remoteOffer, remoteCallerId, remoteCalleeId ->
                onReceivePhoneCallRequest(remoteSessionId, remoteOffer, remoteCallerId, remoteCalleeId)
            },
            onEndCall = {
                onEndCall()
            }
        )
    }
}

class ObservePhoneCallWithInCallUseCase(private val signalingDataUseCase: SendSignalingDataUseCase) {
    suspend operator fun invoke(
        isInCall :  MutableStateFlow<Boolean>,
        calleeId : String,
        onReceivePhoneCallRequest : suspend (sessionId : String,
                                             offer : OfferAnswer,
                                             callerId : String,
                                             calleeId : String) -> Unit,
        onEndCall : suspend () -> Unit) {
        signalingDataUseCase.observePhoneCallWithCheckingInCall(
            isInCall,
            calleeId,
            onReceivePhoneCallRequest = { remoteSessionId, remoteOffer, remoteCallerId, remoteCalleeId ->
                onReceivePhoneCallRequest(remoteSessionId, remoteOffer, remoteCallerId, remoteCalleeId)
            },
            onEndCall = {
                onEndCall()
            }
        )
    }
}

class SetRemoteDescriptionUseCase(private val initializeCallUseCase: InitializeCallUseCase) {
    suspend operator fun invoke(offer : OfferAnswer) {
        initializeCallUseCase.setRemoteDescription(offer)
    }
}

class SendAnswerUseCase(private val initializeCallUseCase: InitializeCallUseCase) {
    suspend operator fun invoke(sessionId : String,
                                offer: OfferAnswer,
                                onSendAnswerResult : (Boolean) -> Unit) {
        val callType = getCallTypeFromSdp(offer.sdp)
        //Create and send answer.
        initializeCallUseCase.createAndSendAnswer(
            sessionId,
            callType,
            null,
            object : Utils.Companion.BasicCallBack{
                override fun onSuccess() {
                    //Send offer success
                    onSendAnswerResult(true)
                }

                override fun onFailure() {
                    //Send offer fail
                    onSendAnswerResult(false)
                }
            }
        )
    }
}

class AcceptCallUseCase(
    private val manageCallStateUseCase: ManageCallStateUseCase,
    private val coroutineScope: CoroutineScope) {
    suspend operator fun invoke(sessionId : String,
                                onAcceptCall : suspend (Boolean) -> Unit) {
        manageCallStateUseCase.acceptCall(
            sessionId,
            object : Utils.Companion.BasicCallBack{
                override fun onSuccess() {
                    coroutineScope.launch {
                        onAcceptCall(true)
                    }
                }

                override fun onFailure() {
                    coroutineScope.launch {
                        onAcceptCall(false)
                    }
                }
            })
    }
}