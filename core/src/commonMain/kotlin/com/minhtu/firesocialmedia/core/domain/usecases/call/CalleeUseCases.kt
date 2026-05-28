package com.minhtu.firesocialmedia.core.domain.usecases.call

import com.minhtu.firesocialmedia.core.domain.entity.call.CallingRequestData
import com.minhtu.firesocialmedia.core.domain.entity.call.IceCandidateData
import com.minhtu.firesocialmedia.core.domain.entity.call.OfferAnswer
import com.minhtu.firesocialmedia.core.utils.Utils
import com.minhtu.firesocialmedia.core.utils.Utils.Companion.getCallTypeFromSdp
import kotlinx.coroutines.flow.MutableStateFlow

data class CalleeUseCases(
    val listenForIncomingCalls : com.minhtu.firesocialmedia.core.domain.usecases.call.ListenForIncomingCallsUseCase,
    val observePhoneCall : com.minhtu.firesocialmedia.core.domain.usecases.call.ObservePhoneCallUseCase,
    val sendIceCandidate: com.minhtu.firesocialmedia.core.domain.usecases.call.SendIceCandidateUseCase,
    val addIceCandidates : com.minhtu.firesocialmedia.core.domain.usecases.call.AddIceCandidatesUseCase,
    val sendAnswer : com.minhtu.firesocialmedia.core.domain.usecases.call.SendAnswerUseCase,
    val setRemoteDescription : com.minhtu.firesocialmedia.core.domain.usecases.call.SetRemoteDescriptionUseCase,
    val acceptCall : com.minhtu.firesocialmedia.core.domain.usecases.call.AcceptCallUseCase,
    val observeVideoCall : com.minhtu.firesocialmedia.core.domain.usecases.call.ObserveVideoCall,
    val endCallUseCase : com.minhtu.firesocialmedia.core.domain.usecases.call.EndCallUseCase,
    val sendWhoEndCallUseCase: com.minhtu.firesocialmedia.core.domain.usecases.call.SendWhoEndCallUseCase
)
class ListenForIncomingCallsUseCase(private val initializeCallUseCase: com.minhtu.firesocialmedia.core.domain.usecases.call.InitializeCallUseCase) {
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

class ObservePhoneCallUseCase(private val signalingDataUseCase: com.minhtu.firesocialmedia.core.domain.usecases.call.SendSignalingDataUseCase) {
    suspend operator fun invoke(
        calleeId : String,
        onReceivePhoneCallRequest : suspend (CallingRequestData) -> Unit,
        iceCandidateCallBack : suspend (iceCandidates : Map<String, IceCandidateData>?) -> Unit,
        onEndCall : suspend () -> Unit,
        whoEndCallCallBack : suspend (String) -> Unit) {
        signalingDataUseCase.observePhoneCallWithoutCheckingInCall(
            calleeId,
            onReceivePhoneCallRequest = { callingRequestData ->
                onReceivePhoneCallRequest(callingRequestData)
            },
            iceCandidateCallBack = { iceCandidates ->
                iceCandidateCallBack(iceCandidates)
            },
            whoEndCallCallBack = { whoEndCall ->
                whoEndCallCallBack(whoEndCall)
            },
            onEndCall = {
                onEndCall()
            }
        )
    }
}

class ObservePhoneCallWithInCallUseCase(private val signalingDataUseCase: com.minhtu.firesocialmedia.core.domain.usecases.call.SendSignalingDataUseCase) {
    suspend operator fun invoke(
        isInCall :  MutableStateFlow<Boolean>,
        calleeId : String,
        onReceivePhoneCallRequest : suspend (CallingRequestData) -> Unit,
        onEndCall : suspend () -> Unit,
        whoEndCallCallBack : suspend (String) -> Unit) {
        signalingDataUseCase.observePhoneCallWithCheckingInCall(
            isInCall,
            calleeId,
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
}

class StopObservePhoneCallUseCase(private val signalingDataUseCase: com.minhtu.firesocialmedia.core.domain.usecases.call.SendSignalingDataUseCase) {
    operator fun invoke() {
        signalingDataUseCase.stopObservePhoneCall()
    }
}

class SetRemoteDescriptionUseCase(private val initializeCallUseCase: com.minhtu.firesocialmedia.core.domain.usecases.call.InitializeCallUseCase) {
    suspend operator fun invoke(offer : OfferAnswer) {
        initializeCallUseCase.setRemoteDescription(offer)
    }
}

class SendAnswerUseCase(private val initializeCallUseCase: com.minhtu.firesocialmedia.core.domain.usecases.call.InitializeCallUseCase) {
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
    private val manageCallStateUseCase: com.minhtu.firesocialmedia.core.domain.usecases.call.ManageCallStateUseCase
) {
    suspend operator fun invoke(sessionId : String) : Boolean {
        return manageCallStateUseCase.acceptCall(sessionId)
    }
}

class AddIceCandidatesUseCase(private val initializeCallUseCase: com.minhtu.firesocialmedia.core.domain.usecases.call.InitializeCallUseCase) {
    suspend operator fun invoke(iceCandidates :Map<String, IceCandidateData>){
        initializeCallUseCase.addIceCandidates(iceCandidates)
    }
}

class SendWhoEndCallUseCase(private val manageCallStateUseCase: com.minhtu.firesocialmedia.core.domain.usecases.call.ManageCallStateUseCase) {
    suspend operator fun invoke(sessionId: String, whoEndCall: String) : Boolean{
        return manageCallStateUseCase.sendWhoEndCall(sessionId, whoEndCall)
    }
}