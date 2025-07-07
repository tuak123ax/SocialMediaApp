package com.minhtu.firesocialmedia.presentation.calling.audiocall

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.minhtu.firesocialmedia.constants.Constants
import com.minhtu.firesocialmedia.data.model.call.AudioCallSession
import com.minhtu.firesocialmedia.data.model.call.CallStatus
import com.minhtu.firesocialmedia.data.model.call.OfferAnswer
import com.minhtu.firesocialmedia.di.PlatformContext
import com.minhtu.firesocialmedia.platform.WebRTCVideoTrack
import com.minhtu.firesocialmedia.platform.logMessage
import com.minhtu.firesocialmedia.utils.Utils
import com.minhtu.firesocialmedia.utils.Utils.Companion.getCallTypeFromSdp
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.math.log

class CallingViewModel(
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) : ViewModel() {
    var sessionId = ""
    fun updateSessionId(id : String) {
        sessionId = id
    }
    fun startCall(callerId: String, calleeId: String, platform : PlatformContext) {
        viewModelScope.launch {
            withContext(ioDispatcher) {
                try {
                    sessionId = generateSessionId(callerId, calleeId)
                    val audioCallSession = AudioCallSession(
                        sessionId = sessionId,
                        callerId = callerId,
                        calleeId = calleeId,
                        status = CallStatus.RINGING)
                    platform.database.sendCallSessionToFirebase(
                        audioCallSession,
                        Constants.CALL_PATH,
                        object : Utils.Companion.BasicCallBack{
                            override fun onSuccess() {
                                //Send call session success
                            }

                            override fun onFailure() {
                                //Send call session fail
                            }

                        })
                    platform.audioCall.initialize(
                        onIceCandidateCreated = { iceCandidateData ->
                            platform.database.sendIceCandidateToFireBase(
                                sessionId,
                                iceCandidateData,
                                "callerCandidates",
                                Constants.CALL_PATH,
                                object : Utils.Companion.BasicCallBack{
                                    override fun onSuccess() {
                                        //Send ice candidate success
                                    }

                                    override fun onFailure() {
                                        //Send ice candidate fail
                                    }

                                }
                            )
                        },
                        onRemoteVideoTrackReceived = { remoteVideoTrack ->
                            remoteVideoTrackState.value = remoteVideoTrack
                        }
                    )
                    platform.audioCall.startCall()
                    platform.audioCall.createOffer(
                        onOfferCreated = { offer ->
                            platform.database.sendOfferToFireBase(
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
                    )
                } catch (e : Exception) {
                    logMessage("startCall Exception", e.message.toString())
                }
            }
        }
    }

    fun stopCall(platform: PlatformContext) {
        try{
            if(sessionId.isNotEmpty()) {
                platform.database.deleteCallSession(
                    sessionId,
                    Constants.CALL_PATH,
                    object : Utils.Companion.BasicCallBack{
                        override fun onSuccess() {
                            //Delete call session success
                        }

                        override fun onFailure() {
                            //Delete call session fail
                        }
                    }
                )
            }
            platform.audioCall.stopCall()
        } catch(e : Exception) {
            logMessage("stopCall Exception", e.message.toString())
        } finally {
            sessionId = ""
        }
    }

    fun generateSessionId(callerId: String, calleeId: String): String {
        return listOf(callerId, calleeId).sorted().joinToString("_")
    }

    private var _remoteVideoTrackState = MutableStateFlow<WebRTCVideoTrack?>(null)
    var remoteVideoTrackState =_remoteVideoTrackState
    fun sendCalleeData(sessionId : String, remoteOffer : OfferAnswer, platform: PlatformContext) {
        try{
            platform.audioCall.initialize(
                onIceCandidateCreated = { iceCandidateData ->
                    platform.database.sendIceCandidateToFireBase(
                        sessionId,
                        iceCandidateData,
                        "calleeCandidates",
                        Constants.CALL_PATH,
                        object : Utils.Companion.BasicCallBack{
                            override fun onSuccess() {
                                //Send ice candidate success
                            }

                            override fun onFailure() {
                                //Send ice candidate fail
                            }

                        }
                    )
                },
                onRemoteVideoTrackReceived = { remoteVideoTrack ->
                    logMessage("onRemoteVideoTrackReceived", "received video")
                    _remoteVideoTrackState.value = remoteVideoTrack
                }
            )
            platform.audioCall.startCall()
            platform.audioCall.setRemoteDescription(remoteOffer)
            val callType = getCallTypeFromSdp(remoteOffer.sdp)
            platform.audioCall.createAnswer(
                callType == CallType.VIDEO,
                onAnswerCreated = { answer ->
                    platform.database.sendAnswerToFirebase(
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
            )
        } catch (e : Exception) {
            logMessage("sendCalleeData Exception", e.message.toString())
        }
    }

    fun observeIceCandidateFromCallee(platform: PlatformContext) {
        viewModelScope.launch {
            withContext(ioDispatcher) {
                platform.database.observeIceCandidatesFromCallee(
                    sessionId,
                    Constants.CALL_PATH,
                    iceCandidateCallBack = { iceCandidate ->
                        if(iceCandidate.candidate != null && iceCandidate.sdpMid != null && iceCandidate.sdpMLineIndex != null) {
                            platform.audioCall.addIceCandidate(iceCandidate.candidate!!, iceCandidate.sdpMid!!, iceCandidate.sdpMLineIndex!!)
                        }
                    })
            }
        }
    }

    fun observeAnswerFromCallee(platform: PlatformContext, onGetAnswerFromCallee : () -> Unit) {
        viewModelScope.launch {
            withContext(ioDispatcher) {
                platform.database.observeAnswerFromCallee(
                    sessionId,
                    Constants.CALL_PATH,
                    answerCallBack = { remoteAnswer ->
                        platform.audioCall.setRemoteDescription(remoteAnswer)
                        onGetAnswerFromCallee()
                    })
            }
        }
    }

    fun requestPermissionAndStartAudioCall(platform: PlatformContext,
                                      onGranted: () -> Unit,
                                      onDenied: () -> Unit) {
        viewModelScope.launch {
            val granted = withContext(Dispatchers.IO) {
                platform.permissionManager.requestAudioPermission()
            }
            if (granted) {
                logMessage("requestPermissionAndStartAudioCall", "granted")
                onGranted()
            } else {
                logMessage("requestPermissionAndStartAudioCall", "not granted")
                onDenied()
            }
        }
    }

    private val _videoCallState = MutableStateFlow<OfferAnswer?>(null)
    val videoCallState = _videoCallState.asStateFlow()
    fun observeVideoCall(sessionId: String,
                         currentUserId : String?,
                         platform : PlatformContext) {
        viewModelScope.launch {
            withContext(ioDispatcher) {
                try{
                    logMessage("observeVideoCall", "start observe: $sessionId")
                    platform.database.observeVideoCall(
                        sessionId,
                        Constants.CALL_PATH,
                        videoCallCallBack = { videoOffer ->
                            logMessage("videoCallCallBack", "currentUserId : $currentUserId")
                            logMessage("videoCallCallBack", videoOffer.initiator)
                            if(currentUserId != null && currentUserId != videoOffer.initiator){
                                _videoCallState.value = videoOffer
                            }
                        })
                } catch(e : Exception){
                    logMessage("observeVideoCall Exception", e.message.toString())
                }
            }
        }
    }

    fun getSessionId(ssId : String) : String {
        return if(ssId.isNotEmpty()) {
            ssId
        } else sessionId
    }
}

enum class CallType {
    AUDIO,
    VIDEO,
    UNKNOWN
}