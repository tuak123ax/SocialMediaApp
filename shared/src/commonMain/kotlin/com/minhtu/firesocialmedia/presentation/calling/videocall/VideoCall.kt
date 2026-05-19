package com.minhtu.firesocialmedia.presentation.calling.videocall

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.Headset
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material.icons.filled.VideocamOff
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.minhtu.firesocialmedia.constants.TestTag
import com.minhtu.firesocialmedia.domain.entity.call.CallEvent
import com.minhtu.firesocialmedia.domain.entity.call.CallEventFlow
import com.minhtu.firesocialmedia.domain.entity.call.OfferAnswer
import com.minhtu.firesocialmedia.domain.entity.call.SpeakerType
import com.minhtu.firesocialmedia.domain.entity.user.UserInstance
import com.minhtu.firesocialmedia.platform.WebRTCVideoView
import com.minhtu.firesocialmedia.platform.logMessage
import com.minhtu.firesocialmedia.platform.showToast
import com.minhtu.firesocialmedia.presentation.loading.Loading
import com.minhtu.firesocialmedia.presentation.loading.LoadingViewModel
import com.minhtu.sharedmodule.ui.theme.activeColor
import com.minhtu.sharedmodule.ui.theme.callStopPendingColor
import com.minhtu.sharedmodule.ui.theme.inactiveColor
import com.minhtu.sharedmodule.ui.theme.videoCallButtonColor
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class VideoCall {
    companion object{
        @Composable
        fun VideoCallScreen(
            sessionId: String,
            caller: UserInstance?,
            callee: UserInstance?,
            currentUserId: String?,
            remoteVideoOffer: OfferAnswer?,
            videoCallViewModel: VideoCallViewModel,
            loadingViewModel: LoadingViewModel,
            onNavigateBack: () -> Unit
        ) {
            // Use ViewModel-stored params when composable params are stale (e.g. after multiple decline/accept)
            val pendingSessionId by videoCallViewModel.pendingVideoCallSessionId
            val pendingOffer by videoCallViewModel.pendingRemoteVideoOffer
            val effectiveSessionId = sessionId.ifEmpty { pendingSessionId }
            val effectiveOffer = remoteVideoOffer ?: pendingOffer
            fun offerKey(offer: OfferAnswer?): String? {
                if (offer == null) return null
                return "${offer.initiator}|${offer.type}|${offer.sdp.hashCode()}"
            }

            // Control button states
            var isMicMuted by remember { mutableStateOf(false) }
            var isCameraOff by remember { mutableStateOf(false) }
            val isLoading by loadingViewModel.isLoading.collectAsState()
            var lastHandledOfferKey by remember { mutableStateOf(offerKey(effectiveOffer)) }
            var isExitPending by remember { mutableStateOf(false) }
            var exitButtonColor by remember { mutableStateOf(videoCallButtonColor) }
            val coroutineScope = rememberCoroutineScope()
            // Video call background is always black; use a white-tinted shadow so buttons
            // stand out from the dark background regardless of the system theme.
            val buttonShadowAmbient = Color.White.copy(alpha = 0.15f)
            val buttonShadowSpot = Color.White.copy(alpha = 0.30f)

            LaunchedEffect(Unit) {
                // Callee path: ensure we don't navigate back due to stale answerVideoCallState from any other flow
                if (effectiveOffer != null) {
                    CallEventFlow.answerVideoCallState.value = true
                }
                videoCallViewModel.requestPermissionsAndStartVideoCall(
                    onGranted = {
                        if (caller != null && callee != null) {
                            loadingViewModel.showLoading()
                            videoCallViewModel.startVideoCall(
                                effectiveOffer,
                                caller,
                                callee,
                                currentUserId,
                                effectiveSessionId
                            )
                            // Persist "video already accepted in this call" so next upgrades auto-join.
                            CallEventFlow.hasAcceptedVideoInCurrentCall.value = true
                            videoCallViewModel.clearPendingVideoCallParams()
                        } else {
                            showToast("Don't have information of caller and callee!")
                            onNavigateBack()
                        }
                    },
                    onDenied = {
                        showToast("Permissions are denied! Return to audio call screen.")
                        onNavigateBack()
                    }
                )
            }

            val videoCallState by CallEventFlow.answerVideoCallState.collectAsState()
            val videoCallDeclinedMessage by CallEventFlow.videoCallDeclinedMessage.collectAsState()
            val incomingVideoOfferState by CallEventFlow.videoCallState.collectAsState()
            LaunchedEffect(videoCallState) {
                if (!videoCallState) {
                    videoCallDeclinedMessage?.let { message ->
                        showToast(message)
                        CallEventFlow.videoCallDeclinedMessage.value = null
                    }
                    CallEventFlow.localVideoTrack.value = null
                    videoCallViewModel.stopVideoCallResources()
                    videoCallViewModel.clearPendingVideoCallParams()
                    CallEventFlow.answerVideoCallState.value = true
                    onNavigateBack()
                } else {
                    videoCallViewModel.updateCameraStatus(isCameraOff)
                }
            }

            // If this screen is still visible (e.g. showing black remote view after peer returned to audio),
            // auto-handle the next incoming video offer so renegotiation resumes without requiring a new popup.
            LaunchedEffect(incomingVideoOfferState?.sdp, incomingVideoOfferState?.initiator) {
                val incomingOffer = incomingVideoOfferState
                val incomingKey = offerKey(incomingOffer)
                if (incomingOffer != null &&
                    incomingOffer.initiator != currentUserId &&
                    incomingKey != null
                ) {
                    // Always consume the videoCallState immediately so the audio screen
                    // (still in the back stack) doesn't re-navigate during the
                    // clearVideoStateAfterNavigate 200ms delay window.
                    CallEventFlow.videoCallState.value = null

                    if (incomingKey != lastHandledOfferKey) {
                        if (caller != null && callee != null && effectiveSessionId.isNotEmpty()) {
                            loadingViewModel.showLoading()
                            lastHandledOfferKey = incomingKey
                            videoCallViewModel.startVideoCall(
                                incomingOffer,
                                caller,
                                callee,
                                currentUserId,
                                effectiveSessionId
                            )
                            CallEventFlow.hasAcceptedVideoInCurrentCall.value = true
                        }
                    }
                }
            }

            val callEventState by CallEventFlow.events.collectAsState()
            LaunchedEffect(callEventState) {
                if(callEventState != null) {
                    logMessage("VideoCallScreen", { callEventState.toString() })
                    when(callEventState) {
                        CallEvent.StopVideoCall -> {
                            logMessage("VideoCallScreen", { "StopVideoCall" })
                            onNavigateBack()
                        }

                        else -> {}
                    }
                }
            }

            val localVideoTrackState = CallEventFlow.localVideoTrack.collectAsState(initial = null)
            val remoteVideoTrackState = CallEventFlow.remoteVideoTrack.collectAsState(initial = null)

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black)
            ) {

                if (localVideoTrackState.value != null && remoteVideoTrackState.value != null) {
                    loadingViewModel.hideLoading()
                }

                WebRTCVideoView(
                    localVideoTrackState.value,
                    remoteVideoTrackState.value,
                    isCameraOff,
                    modifier = Modifier.fillMaxSize()
                )

                if (isLoading) {
                    Loading.LoadingScreen()
                }

                // Bottom control buttons
                Row(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 40.dp),
                    horizontalArrangement = Arrangement.spacedBy(24.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    var speakerType by remember { mutableStateOf<SpeakerType>(SpeakerType.Audio) }

                    // Mic Button
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(56.dp)
                            .shadow(8.dp, CircleShape, ambientColor = buttonShadowAmbient, spotColor = buttonShadowSpot)
                            .clip(CircleShape)
                            .background(if (isMicMuted) inactiveColor else activeColor)
                    ) {
                        FloatingActionButton(
                            onClick = {
                                if (!isExitPending) {
                                    isMicMuted = !isMicMuted
                                    videoCallViewModel.updateMicStatus(isMicMuted)
                                }
                            },
                            containerColor = Color.Transparent,
                            elevation = FloatingActionButtonDefaults.elevation(0.dp)
                        ) {
                            Icon(
                                imageVector = if (isMicMuted)
                                    Icons.Default.MicOff
                                else
                                    Icons.Default.Mic,
                                contentDescription = "Toggle Mic",
                                tint = Color.White
                            )
                        }
                    }

                    // Speaker Button
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(56.dp)
                            .shadow(8.dp, CircleShape, ambientColor = buttonShadowAmbient, spotColor = buttonShadowSpot)
                            .clip(CircleShape)
                            .background(
                                if (speakerType == SpeakerType.Speaker)
                                    activeColor
                                else
                                    inactiveColor
                            )
                    ) {
                        FloatingActionButton(
                            onClick = {
                                if (!isExitPending) {
                                    speakerType =
                                        if (speakerType == SpeakerType.Audio)
                                            SpeakerType.Speaker
                                        else
                                            SpeakerType.Audio
                                    videoCallViewModel.updateSpeakerStatus(speakerType)
                                }
                            },
                            containerColor = Color.Transparent,
                            elevation = FloatingActionButtonDefaults.elevation(0.dp)
                        ) {
                            Icon(
                                imageVector = if (speakerType == SpeakerType.Speaker)
                                    Icons.Default.VolumeUp
                                else
                                    Icons.Default.Headset,
                                contentDescription = "Toggle Speaker",
                                tint = Color.White
                            )
                        }
                    }

                    // Exit Button
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(56.dp)
                            .shadow(8.dp, CircleShape, ambientColor = buttonShadowAmbient, spotColor = buttonShadowSpot)
                            .clip(CircleShape)
                            .background(exitButtonColor)
                            .testTag(TestTag.TAG_BUTTON_EXIT_VIDEO_CALL)
                            .semantics {
                                contentDescription = TestTag.TAG_BUTTON_EXIT_VIDEO_CALL
                            }
                    ) {
                        FloatingActionButton(
                            onClick = {
                                if (!isExitPending) {
                                    isExitPending = true
                                    exitButtonColor = callStopPendingColor
                                    coroutineScope.launch {
                                        delay(500L)
                                        logMessage("ClickBack", { "Back to audio screen" })
                                        CallEventFlow.localVideoTrack.value = null
                                        videoCallViewModel.stopVideoCallResources()
                                        videoCallViewModel.clearPendingVideoCallParams()
                                        onNavigateBack()
                                    }
                                }
                            },
                            containerColor = Color.Transparent,
                            elevation = FloatingActionButtonDefaults.elevation(0.dp)
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ExitToApp,
                                contentDescription = "Back",
                                tint = Color.White
                            )
                        }
                    }

                    // Camera Button
                    val hasLocalVideoTrack = localVideoTrackState.value != null
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(56.dp)
                            .shadow(8.dp, CircleShape, ambientColor = buttonShadowAmbient, spotColor = buttonShadowSpot)
                            .clip(CircleShape)
                            .background(
                                if (!hasLocalVideoTrack) videoCallButtonColor
                                else if (isCameraOff) inactiveColor
                                else activeColor
                            )
                    ) {
                        FloatingActionButton(
                            onClick = {
                                if (!isExitPending) {
                                    isCameraOff = !isCameraOff
                                    videoCallViewModel.updateCameraStatus(isCameraOff)
                                }
                            },
                            containerColor = Color.Transparent,
                            elevation = FloatingActionButtonDefaults.elevation(0.dp)
                        ) {
                            Icon(
                                imageVector = if (isCameraOff)
                                    Icons.Default.VideocamOff
                                else
                                    Icons.Default.Videocam,
                                contentDescription = "Toggle Camera",
                                tint = Color.White
                            )
                        }
                    }
                }
            }
        }

        fun getScreenName() : String {
            return "VideoCallScreen"
        }
    }
}