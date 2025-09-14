package com.minhtu.firesocialmedia.domain.usecases.call

import com.minhtu.firesocialmedia.domain.entity.call.AudioCallSession
import com.minhtu.firesocialmedia.domain.entity.call.CallStatus
import com.minhtu.firesocialmedia.domain.entity.call.IceCandidateData
import com.minhtu.firesocialmedia.domain.entity.call.OfferAnswer
import com.minhtu.firesocialmedia.domain.repository.CallRepository
import com.minhtu.firesocialmedia.platform.logMessage
import com.minhtu.firesocialmedia.utils.Utils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch

class SendSignalingDataUseCase(
    val callRepository: CallRepository,
    val coroutineScope: CoroutineScope = CoroutineScope(Dispatchers.IO)
) {
    suspend fun sendCallSessionToFirebase(audioCallSession : AudioCallSession,
                                  sendCallSessionCallBack : Utils.Companion.BasicCallBack){
        callRepository.sendCallSessionToFirebase(
            audioCallSession,
            object : Utils.Companion.BasicCallBack{
                override fun onSuccess() {
                    //Send call session success
                    sendCallSessionCallBack.onSuccess()
                }

                override fun onFailure() {
                    //Send call session fail
                    logMessage("sendCallSessionToFirebase", {"send call session fail"})
                    sendCallSessionCallBack.onFailure()
                }

            }
        )
    }

     suspend fun observeIceCandidateFromCallee(sessionId : String) {
         logMessage("observeIceCandidateFromCallee", { "observe in service" })
         callRepository.observeIceCandidatesFromCallee(
             sessionId,
             iceCandidateCallBack = { iceCandidate ->
                 //Add ice candidates to peer connection when received.
                 if(iceCandidate.candidate != null && iceCandidate.sdpMid != null && iceCandidate.sdpMLineIndex != null) {
                     coroutineScope.launch {
                         logMessage("iceCandidateCallBack",
                             { "add ice candidate for caller" })
                         callRepository.addIceCandidate(iceCandidate.candidate!!, iceCandidate.sdpMid!!, iceCandidate.sdpMLineIndex!!)
                     }
                 }
             }
         )
    }

    suspend fun observeAnswerFromCallee(sessionId : String,
                                callerId : String?,
                                onGetAnswerFromCallee : () -> Unit,
                                onRejectVideoCall: suspend () -> Unit) {
        logMessage("observeAnswerFromCallee", { "observe in service" })
        //Observe answer from callee.
        callRepository.observeAnswerFromCallee(
            sessionId,
            answerCallBack = { remoteAnswer ->
                //Set remote description when received answer from callee.
                coroutineScope.launch {
                    if(callerId != null && callerId != remoteAnswer.initiator)
                        logMessage("observeAnswerFromCallee", { "setRemoteDescription" })
                    callRepository.setRemoteDescription(remoteAnswer)
                }
                onGetAnswerFromCallee()
            },
            rejectCallBack = {
                //Update offer data on DB in case reject video call.
                coroutineScope.launch {
                    logMessage("observeAnswerFromCallee", { "reject video call" })
                    callRepository.updateOfferInFirebase(
                        sessionId,
                        "",
                        "initiator",
                        object : Utils.Companion.BasicCallBack{
                            override fun onSuccess() {
                                //Send offer success
                            }

                            override fun onFailure() {
                                //Send offer fail
                            }

                        }
                    )
                    onRejectVideoCall()
                }
            }
        )
    }

    suspend fun observeCallStatus(sessionId : String,
                          onAcceptCall : suspend () -> Unit,
                          onEndCall : suspend () -> Unit) {
        //Observe call status via DB
        callRepository.observeCallStatus(
            sessionId,
            object : Utils.Companion.CallStatusCallBack{
                override fun onSuccess(status : CallStatus) {
                    coroutineScope.launch {
                        if(status == CallStatus.ACCEPTED) {
                            //Call is accepted
                            onAcceptCall()
                        }
                    }
                }

                override fun onFailure() {
                    coroutineScope.launch {
                        onEndCall()
                    }
                }
            }
        )
    }

    suspend fun updateAnswerInFirebase(sessionId : String) {
        //Update initiator value to Reject so that other user can observe it.
        callRepository.updateAnswerInFirebase(
            sessionId,
            "Reject",
            "initiator",
            object : Utils.Companion.BasicCallBack{
                override fun onSuccess() {
                    //Send offer success
                }

                override fun onFailure() {
                    //Send offer fail
                }

            }
        )
    }

    suspend fun sendOfferToFireBase(sessionId : String, offer : OfferAnswer) {
        callRepository.sendOfferToFireBase(
            sessionId,
            offer,
            object : Utils.Companion.BasicCallBack{
                override fun onSuccess() {
                    //Send offer success
                }

                override fun onFailure() {
                    //Send offer fail
                }

            }
        )
    }

    suspend fun observePhoneCallWithoutCheckingInCall(
        calleeIdFromFCM : String,
        onReceivePhoneCallRequest : suspend (sessionId : String,
                                             offer : OfferAnswer,
                                             callerId : String,
                                             calleeId : String) -> Unit,
        onEndCall: suspend () -> Unit) {
        callRepository.observePhoneCallWithoutCheckingInCall(
            calleeIdFromFCM,
            phoneCallCallBack = { remoteSessionId,remoteCallerId, remoteCalleeId, remoteOffer ->
                //Received phone call request.
                logMessage("observePhoneCallWithoutCheckingInCall",
                    { "phoneCallCallBack" })
                if(remoteCalleeId == calleeIdFromFCM) {
                    coroutineScope.launch {
                        onReceivePhoneCallRequest(remoteSessionId, remoteOffer, remoteCallerId, remoteCalleeId)
                    }
                }
            },
            endCallSession = { end ->
                if(end) {
                    //Handle end call.
                    coroutineScope.launch {
                        onEndCall()
                    }
                }
            },
            iceCandidateCallBack = { iceCandidates ->
                //Add ice candidates to peer connection.
                logMessage("observePhoneCallWithoutCheckingInCall",
                    { "iceCandidateCallBack" })
                if(iceCandidates != null) {
                    for(candidate in iceCandidates.values) {
                        if(candidate.candidate != null && candidate.sdpMid != null && candidate.sdpMLineIndex != null) {
                            coroutineScope.launch {
                                logMessage("iceCandidateCallBack",
                                    { "add ice candidate for callee" })
                                callRepository.addIceCandidate(candidate.candidate!!, candidate.sdpMid!!, candidate.sdpMLineIndex!!)
                            }
                        }
                    }
                }
            }
        )
    }

    suspend fun observePhoneCallWithCheckingInCall(
        isInCall :  MutableStateFlow<Boolean>,
        currentUserId : String,
        onReceivePhoneCallRequest : suspend (sessionId : String,
                                             offer : OfferAnswer,
                                             callerId : String,
                                             calleeId : String) -> Unit,
        onEndCall: suspend () -> Unit) {
        callRepository.observePhoneCall(
            isInCall,
            currentUserId,
            phoneCallCallBack = { sessionId,callerId, calleeId, offer ->
                if(calleeId == currentUserId) {
                    coroutineScope.launch {
                        onReceivePhoneCallRequest(sessionId, offer, callerId, calleeId)
                    }
                }
            },
            endCallSession = { end ->
                if(end) {
                    //Handle end call.
                    coroutineScope.launch {
                        onEndCall()
                    }
                }
            },
            iceCandidateCallBack = { iceCandidates ->
                if(iceCandidates != null) {
                    for(candidate in iceCandidates.values) {
                        if(candidate.candidate != null && candidate.sdpMid != null && candidate.sdpMLineIndex != null) {
                            coroutineScope.launch {
                                callRepository.addIceCandidate(candidate.candidate!!, candidate.sdpMid!!, candidate.sdpMLineIndex!!)
                            }
                        }
                    }
                }
            }
        )
    }

    suspend fun sendAnswerToFirebase(sessionId : String, answer: OfferAnswer) {
        callRepository.sendAnswerToFirebase(
            sessionId,
            answer,
            object : Utils.Companion.BasicCallBack{
                override fun onSuccess() {
                    //Send offer success
                }

                override fun onFailure() {
                    //Send offer fail
                }
            }
        )
    }

    suspend fun sendIceCandidateToFireBase(sessionId : String,
                                           iceCandidate: IceCandidateData,
                                           whichCandidate : String,
                                           sendIceCandidateCallBack : Utils.Companion.BasicCallBack) {
        callRepository.sendIceCandidateToFireBase(
            sessionId,
            iceCandidate,
            whichCandidate,
            sendIceCandidateCallBack
        )
    }
}