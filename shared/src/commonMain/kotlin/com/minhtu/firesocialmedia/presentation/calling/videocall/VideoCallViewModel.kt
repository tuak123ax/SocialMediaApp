package com.minhtu.firesocialmedia.presentation.calling.videocall

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.minhtu.firesocialmedia.constants.Constants
import com.minhtu.firesocialmedia.data.model.call.OfferAnswer
import com.minhtu.firesocialmedia.di.PlatformContext
import com.minhtu.firesocialmedia.platform.WebRTCVideoTrack
import com.minhtu.firesocialmedia.platform.logMessage
import com.minhtu.firesocialmedia.presentation.calling.audiocall.CallType
import com.minhtu.firesocialmedia.utils.Utils
import com.minhtu.firesocialmedia.utils.Utils.Companion.getCallTypeFromSdp
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class VideoCallViewModel(
    val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) : ViewModel() {
    private val _localVideoTrackState = MutableStateFlow<WebRTCVideoTrack?>(null)
    val localVideoTrackState = _localVideoTrackState.asStateFlow()
    fun startVideoCall(
        remoteVideoOffer : OfferAnswer?,
        currentUserId : String?,
        sessionId : String,
        platform : PlatformContext) {
        logMessage("startVideoCall", sessionId)
        viewModelScope.launch {
            withContext(ioDispatcher) {
                try {
                    platform.audioCall.startVideoCall(
                        onStartVideoCall = { videoTrack ->
                            _localVideoTrackState.value = videoTrack
                        }
                    )
                    if(remoteVideoOffer == null) {
                        platform.audioCall.createVideoOffer(
                            onOfferCreated = { offer ->
                                if(currentUserId != null) {
                                    offer.initiator = currentUserId
                                }
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
                    } else {
                        platform.audioCall.setRemoteDescription(remoteVideoOffer)
                        val callType = getCallTypeFromSdp(remoteVideoOffer.sdp)
                        platform.audioCall.createAnswer(
                            callType == CallType.VIDEO,
                            onAnswerCreated  = { answer ->
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
                    }
                } catch (e : Exception) {
                    logMessage("startVideoCall Exception", e.message.toString())
                }
            }
        }
    }

    fun requestPermissionsAndStartVideoCall(
        platform: PlatformContext,
        onGranted: () -> Unit,
        onDenied: () -> Unit) {
        viewModelScope.launch {
            val granted = withContext(Dispatchers.IO) {
                platform.permissionManager.requestCameraAndAudioPermissions()
            }
            if (granted) {
                onGranted()
            } else {
                onDenied()
            }
        }
    }
}