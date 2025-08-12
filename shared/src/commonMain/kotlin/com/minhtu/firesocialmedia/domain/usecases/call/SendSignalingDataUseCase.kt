package com.minhtu.firesocialmedia.domain.usecases.call

import com.minhtu.firesocialmedia.constants.Constants
import com.minhtu.firesocialmedia.data.model.call.AudioCallSession
import com.minhtu.firesocialmedia.data.model.call.CallStatus
import com.minhtu.firesocialmedia.data.model.call.IceCandidateData
import com.minhtu.firesocialmedia.data.model.call.OfferAnswer
import com.minhtu.firesocialmedia.domain.serviceimpl.call.AudioCallService
import com.minhtu.firesocialmedia.domain.serviceimpl.database.DatabaseService
import com.minhtu.firesocialmedia.platform.logMessage
import com.minhtu.firesocialmedia.utils.Utils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

class SendSignalingDataUseCase(
    val audioCallService: AudioCallService,
    val databaseService: DatabaseService,
    val coroutineScope: CoroutineScope
) {
    suspend fun sendCallSessionToFirebase(audioCallSession : AudioCallSession,
                                  sendCallSessionCallBack : Utils.Companion.BasicCallBack){
        databaseService.sendCallSessionToFirebase(
            audioCallSession,
            Constants.CALL_PATH,
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

            })
    }

     suspend fun observeIceCandidateFromCallee(sessionId : String) {
         logMessage("observeIceCandidateFromCallee", { "observe in service" })
         databaseService.observeIceCandidatesFromCallee(
             sessionId,
             Constants.CALL_PATH,
             iceCandidateCallBack = { iceCandidate ->
                 //Add ice candidates to peer connection when received.
                 if(iceCandidate.candidate != null && iceCandidate.sdpMid != null && iceCandidate.sdpMLineIndex != null) {
                     coroutineScope.launch {
                         logMessage("iceCandidateCallBack",
                             { "add ice candidate for caller" })
                         audioCallService.addIceCandidate(iceCandidate.candidate!!, iceCandidate.sdpMid!!, iceCandidate.sdpMLineIndex!!)
                     }
                 }
             })
    }

    suspend fun observeAnswerFromCallee(sessionId : String,
                                callerId : String?,
                                onGetAnswerFromCallee : () -> Unit,
                                onRejectVideoCall: suspend () -> Unit) {
        logMessage("observeAnswerFromCallee", { "observe in service" })
        //Observe answer from callee.
        databaseService.observeAnswerFromCallee(
            sessionId,
            Constants.CALL_PATH,
            answerCallBack = { remoteAnswer ->
                //Set remote description when received answer from callee.
                coroutineScope.launch {
                    if(callerId != null && callerId != remoteAnswer.initiator)
                        logMessage("observeAnswerFromCallee", { "setRemoteDescription" })
                        audioCallService.setRemoteDescription(remoteAnswer)
                }
                onGetAnswerFromCallee()
            },
            rejectCallBack = {
                //Update offer data on DB in case reject video call.
                coroutineScope.launch {
                    logMessage("observeAnswerFromCallee", { "reject video call" })
                    databaseService.updateOfferInFirebase(
                        sessionId,
                        "",
                        "initiator",
                        Constants.CALL_PATH,
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
        databaseService.observeCallStatus(
            sessionId,
            Constants.CALL_PATH,
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
        databaseService.updateAnswerInFirebase(
            sessionId,
            "Reject",
            "initiator",
            Constants.CALL_PATH,
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
        databaseService.sendOfferToFireBase(
            sessionId,
            offer,
            Constants.CALL_PATH,
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
        databaseService.observePhoneCallWithoutCheckingInCall(
            calleeIdFromFCM,
            Constants.CALL_PATH,
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
                                audioCallService.addIceCandidate(candidate.candidate!!, candidate.sdpMid!!, candidate.sdpMLineIndex!!)
                            }
                        }
                    }
                }
            })
    }

    suspend fun sendAnswerToFirebase(sessionId : String, answer: OfferAnswer) {
        databaseService.sendAnswerToFirebase(
            sessionId,
            answer,
            Constants.CALL_PATH,
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
                                           callPath : String,
                                           sendIceCandidateCallBack : Utils.Companion.BasicCallBack) {
        databaseService.sendIceCandidateToFireBase(
            sessionId,
            iceCandidate,
            whichCandidate,
            callPath,
            sendIceCandidateCallBack)
    }
}