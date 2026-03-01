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
import com.minhtu.firesocialmedia.domain.entity.call.CallEventFlow
import com.minhtu.firesocialmedia.domain.entity.call.OfferAnswer
import com.minhtu.firesocialmedia.domain.entity.call.SpeakerType
import com.minhtu.firesocialmedia.domain.entity.user.UserInstance
import com.minhtu.firesocialmedia.platform.WebRTCVideoView
import com.minhtu.firesocialmedia.platform.showToast
import com.minhtu.firesocialmedia.presentation.loading.Loading
import com.minhtu.firesocialmedia.presentation.loading.LoadingViewModel
import com.minhtu.firesocialmedia.utils.NavigationHandler
import com.minhtu.sharedmodule.ui.theme.activeColor
import com.minhtu.sharedmodule.ui.theme.inactiveColor

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
            navHandler: NavigationHandler
        ) {
            // Control button states
            var isMicMuted by remember { mutableStateOf(false) }
            var isCameraOff by remember { mutableStateOf(false) }
            val isLoading by loadingViewModel.isLoading.collectAsState()

            LaunchedEffect(Unit) {
                videoCallViewModel.requestPermissionsAndStartVideoCall(
                    onGranted = {
                        if (caller != null && callee != null) {
                            loadingViewModel.showLoading()
                            videoCallViewModel.startVideoCall(
                                remoteVideoOffer,
                                caller,
                                callee,
                                currentUserId,
                                sessionId
                            )
                        } else {
                            showToast("Don't have information of caller and callee!")
                            navHandler.navigateBack()
                        }
                    },
                    onDenied = {
                        showToast("Permissions are denied! Return to audio call screen.")
                        navHandler.navigateBack()
                    }
                )
            }

            val videoCallState by CallEventFlow.answerVideoCallState.collectAsState()
            LaunchedEffect(videoCallState) {
                if (!videoCallState) {
                    CallEventFlow.answerVideoCallState.value = true
                    navHandler.navigateBack()
                } else {
                    videoCallViewModel.updateCameraStatus(isCameraOff)
                }
            }

            val localVideoTrackState = CallEventFlow.localVideoTrack.collectAsState(initial = null)
            val remoteVideoTrackState = CallEventFlow.remoteVideoTrack.collectAsState(initial = null)

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.White)
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
                            .shadow(8.dp, CircleShape)
                            .clip(CircleShape)
                            .background(if (isMicMuted) inactiveColor else activeColor)
                    ) {
                        FloatingActionButton(
                            onClick = {
                                isMicMuted = !isMicMuted
                                videoCallViewModel.updateMicStatus(isMicMuted)
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
                            .shadow(8.dp, CircleShape)
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
                                speakerType =
                                    if (speakerType == SpeakerType.Audio)
                                        SpeakerType.Speaker
                                    else
                                        SpeakerType.Audio

                                videoCallViewModel.updateSpeakerStatus(speakerType)
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
                            .shadow(8.dp, CircleShape)
                            .clip(CircleShape)
                            .background(Color(0xFF3A3A3C))
                            .testTag(TestTag.TAG_BUTTON_EXIT_VIDEO_CALL)
                            .semantics {
                                contentDescription = TestTag.TAG_BUTTON_EXIT_VIDEO_CALL
                            }
                    ) {
                        FloatingActionButton(
                            onClick = {
                                videoCallViewModel.updateCameraStatus(true)
                                navHandler.navigateBack() },
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
                            .shadow(8.dp, CircleShape)
                            .clip(CircleShape)
                            .background(
                                if (!hasLocalVideoTrack) Color.Gray
                                else if (isCameraOff) inactiveColor
                                else activeColor
                            )
                    ) {
                        FloatingActionButton(
                            onClick = {
                                isCameraOff = !isCameraOff
                                videoCallViewModel.updateCameraStatus(isCameraOff)
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