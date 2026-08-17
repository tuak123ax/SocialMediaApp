package com.minhtu.firesocialmedia.domain.usecases.call

import com.minhtu.firesocialmedia.domain.entity.call.AudioCallSession
import com.minhtu.firesocialmedia.domain.entity.call.CallStatus
import com.minhtu.firesocialmedia.domain.entity.call.CallStatusCallBack
import com.minhtu.firesocialmedia.domain.entity.call.CallingRequestData
import com.minhtu.firesocialmedia.domain.entity.call.IceCandidateData
import com.minhtu.firesocialmedia.domain.entity.call.OfferAnswer
import com.minhtu.firesocialmedia.domain.repository.CallRepository
import com.minhtu.firesocialmedia.calling.utils.Utils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch

class SendSignalingDataUseCase(
    val callRepository: CallRepository,
    val coroutineScope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
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
                    sendCallSessionCallBack.onFailure()
                }

            }
        )
    }

     suspend fun observeIceCandidateFromCallee(sessionId : String) {
         callRepository.observeIceCandidatesFromCallee(
             sessionId,
             iceCandidateCallBack = { iceCandidate ->
                 //Add ice candidates to peer connection when received.
                 if(iceCandidate.candidate != null && iceCandidate.sdpMid != null && iceCandidate.sdpMLineIndex != null) {
                     coroutineScope.launch {
                         callRepository.addIceCandidate(iceCandidate.candidate!!, iceCandidate.sdpMid!!, iceCandidate.sdpMLineIndex!!)
                     }
                 }
             }
         )
    }

    suspend fun observeAnswerFromCallee(
        sessionId : String,
        callerId : String?,
        expectVideoAnswer: Boolean = false,
        onGetAnswerFromCallee : () -> Unit,
        onRejectVideoCall: suspend () -> Unit
    ) {
        //Observe answer from callee.
        callRepository.observeAnswerFromCallee(
            sessionId,
            answerCallBack = answerObserver@ { remoteAnswer ->
                val initiator = remoteAnswer.initiator
                val isVideoAnswer = remoteAnswer.sdp?.contains("m=video") == true

                // Ignore answers authored by this device. This protects the callee side from any
                // stale answer observer that might still be attached during video upgrade.
                if (!callerId.isNullOrEmpty() && initiator == callerId) {
                    return@answerObserver
                }

                // When starting a video upgrade, the old audio answer may still be present in the
                // shared Firebase answer node. Ignore that stale audio answer and wait for the
                // fresh video answer, otherwise the peer connection goes back to STABLE too early.
                if (expectVideoAnswer && !isVideoAnswer) {
                    return@answerObserver
                }

                coroutineScope.launch {
                    callRepository.setRemoteDescription(remoteAnswer)
                    onGetAnswerFromCallee()
                }
            },
            rejectCallBack = {
                //Update offer data on DB in case reject video call.
                coroutineScope.launch {
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
            object : CallStatusCallBack{
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

    /** Clear the answer node so a new video offer attempt is not hit by a stale "Reject" from a previous decline. */
    suspend fun clearAnswerInFirebaseForNewVideoOffer(sessionId : String) {
        callRepository.clearAnswerInFirebase(sessionId)
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
        onReceivePhoneCallRequest : suspend (CallingRequestData) -> Unit,
        iceCandidateCallBack : suspend (iceCandidates : Map<String, IceCandidateData>?) -> Unit,
        onEndCall: suspend () -> Unit,
        whoEndCallCallBack : suspend (String) -> Unit) {
        callRepository.observePhoneCallWithoutCheckingInCall(
            calleeIdFromFCM,
            phoneCallCallBack = { callingRequestData ->
                //Received phone call request.
                if(callingRequestData.calleeId == calleeIdFromFCM) {
                    coroutineScope.launch {
                        onReceivePhoneCallRequest(callingRequestData)
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
            whoEndCallCallBack = { whoEndCall ->
                coroutineScope.launch {
                    whoEndCallCallBack(whoEndCall)
                }
            },
            iceCandidateCallBack = { iceCandidates ->
                //Add ice candidates to peer connection.
                coroutineScope.launch{
                    iceCandidateCallBack(iceCandidates)
                }
            }
        )
    }

    suspend fun observePhoneCallWithCheckingInCall(
        isInCall :  MutableStateFlow<Boolean>,
        currentUserId : String,
        onReceivePhoneCallRequest : suspend (CallingRequestData) -> Unit,
        onEndCall: suspend () -> Unit,
        whoEndCallCallBack : suspend (String) -> Unit) {
        callRepository.observePhoneCall(
            isInCall,
            currentUserId,
            phoneCallCallBack = { callingRequestData ->
                if(callingRequestData.calleeId == currentUserId) {
                    coroutineScope.launch {
                        onReceivePhoneCallRequest(callingRequestData)
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
            whoEndCallCallBack = { whoEndCall ->
                coroutineScope.launch {
                    whoEndCallCallBack(whoEndCall)
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

    fun stopObservePhoneCall() {
        callRepository.stopObservePhoneCall()
    }
}
