package com.minhtu.firesocialmedia.domain.usecases.call

import com.minhtu.firesocialmedia.domain.entity.call.CallingRequestData
import com.minhtu.firesocialmedia.domain.entity.call.IceCandidateData
import com.minhtu.firesocialmedia.domain.entity.call.OfferAnswer
import com.minhtu.firesocialmedia.utils.Utils
import com.minhtu.firesocialmedia.utils.Utils.Companion.getCallTypeFromSdp
import kotlinx.coroutines.flow.MutableStateFlow

data class CalleeUseCases(
    val listenForIncomingCalls : ListenForIncomingCallsUseCase,
    val observePhoneCall : ObservePhoneCallUseCase,
    val sendIceCandidate: SendIceCandidateUseCase,
    val addIceCandidates : AddIceCandidatesUseCase,
    val sendAnswer : SendAnswerUseCase,
    val setRemoteDescription : SetRemoteDescriptionUseCase,
    val acceptCall : AcceptCallUseCase,
    val observeVideoCall : ObserveVideoCall,
    val endCallUseCase : EndCallUseCase,
    val sendWhoEndCallUseCase: SendWhoEndCallUseCase
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

class ObservePhoneCallWithInCallUseCase(private val signalingDataUseCase: SendSignalingDataUseCase) {
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

class StopObservePhoneCallUseCase(private val signalingDataUseCase: SendSignalingDataUseCase) {
    operator fun invoke() {
        signalingDataUseCase.stopObservePhoneCall()
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
    private val manageCallStateUseCase: ManageCallStateUseCase) {
    suspend operator fun invoke(sessionId : String) : Boolean {
        return manageCallStateUseCase.acceptCall(sessionId)
    }
}

class AddIceCandidatesUseCase(private val initializeCallUseCase: InitializeCallUseCase) {
    suspend operator fun invoke(iceCandidates :Map<String, IceCandidateData>){
        initializeCallUseCase.addIceCandidates(iceCandidates)
    }
}

class SendWhoEndCallUseCase(private val manageCallStateUseCase: ManageCallStateUseCase) {
    suspend operator fun invoke(sessionId: String, whoEndCall: String) : Boolean{
        return manageCallStateUseCase.sendWhoEndCall(sessionId, whoEndCall)
    }
}