package com.minhtu.firesocialmedia.presentation.calling.videocall

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CallEnd
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
import com.minhtu.firesocialmedia.domain.entity.user.UserInstance
import com.minhtu.firesocialmedia.platform.WebRTCVideoView
import com.minhtu.firesocialmedia.platform.showToast
import com.minhtu.firesocialmedia.presentation.loading.Loading
import com.minhtu.firesocialmedia.presentation.loading.LoadingViewModel
import com.minhtu.firesocialmedia.utils.NavigationHandler

class VideoCall {
    companion object{
        @Composable
        fun VideoCallScreen(
            sessionId : String,
            caller : UserInstance?,
            callee : UserInstance?,
            currentUserId : String?,
            remoteVideoOffer : OfferAnswer?,
            videoCallViewModel: VideoCallViewModel,
            loadingViewModel: LoadingViewModel,
            navHandler : NavigationHandler,
            modifier: Modifier
        ) {
            val isLoading by loadingViewModel.isLoading.collectAsState()
            LaunchedEffect(Unit) {
                videoCallViewModel.requestPermissionsAndStartVideoCall(
                    onGranted = {
                        if(caller != null && callee != null) {
                            loadingViewModel.showLoading()
                            videoCallViewModel.startVideoCall(
                                remoteVideoOffer,
                                caller,
                                callee,
                                currentUserId,
                                sessionId)
                        } else {
                            showToast("Don't have information of caller and callee!")
                            navHandler.navigateBack()
                        }
                    },
                    onDenied = {
                        showToast("Permissions are denied! Return to audio call screen.")
                        navHandler.navigateBack()
                    })
            }

            //Observe answer from callee
            val videoCallState by CallEventFlow.answerVideoCallState.collectAsState()
            LaunchedEffect(videoCallState) {
                if(!videoCallState) {
                    CallEventFlow.answerVideoCallState.value = true
                    navHandler.navigateBack()
                }
            }

            //Observe local and remote video tracks
            val localVideoTrackState = CallEventFlow.localVideoTrack.collectAsState(initial = null)
            val remoteVideoTrackState = CallEventFlow.remoteVideoTrack.collectAsState(initial = null)
            Box(modifier = modifier) {
                //Video call screen includes: local video screen and remote video screen
                if(localVideoTrackState.value != null && remoteVideoTrackState.value != null) {
                    loadingViewModel.hideLoading()
                }
                WebRTCVideoView(
                    localVideoTrackState.value,
                    remoteVideoTrackState.value,
                    modifier = Modifier.fillMaxSize()
                )
                if (isLoading) {
                    Loading.Companion.LoadingScreen()
                }

                //Button stop video call
                var backgroundButton by remember {mutableStateOf(Color.Red)}
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(56.dp)
                        .shadow(8.dp, CircleShape) // Shadow before clipping
                        .clip(CircleShape)
                        .background(backgroundButton)
                        .testTag(TestTag.TAG_REJECT_CALL_BUTTON)
                        .semantics {
                            contentDescription = TestTag.TAG_REJECT_CALL_BUTTON
                        }
                        .align(Alignment.BottomCenter)
                ) {
                    FloatingActionButton(
                        onClick = {
                            backgroundButton = Color.Gray
                            navHandler.navigateBack()
                        },
                        shape = CircleShape,
                        containerColor = Color.Transparent, // Transparent to let gradient show
                        elevation = FloatingActionButtonDefaults.elevation(0.dp)
                    ) {
                        Icon(Icons.Default.CallEnd, contentDescription = "Stop Call", tint = Color.Black)
                    }
                }
            }
        }

        fun getScreenName() : String {
            return "VideoCallScreen"
        }
    }
}