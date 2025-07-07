package com.minhtu.firesocialmedia.presentation.calling.videocall

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import com.minhtu.firesocialmedia.data.model.call.OfferAnswer
import com.minhtu.firesocialmedia.di.PlatformContext
import com.minhtu.firesocialmedia.platform.WebRTCVideoView
import com.minhtu.firesocialmedia.platform.showToast
import com.minhtu.firesocialmedia.presentation.calling.audiocall.CallingViewModel
import com.minhtu.firesocialmedia.utils.NavigationHandler
import com.minhtu.firesocialmedia.utils.Utils.Companion.stopCallAction

class VideoCall {
    companion object{
        @Composable
        fun VideoCallScreen(
            sessionId : String,
            currentUserId : String?,
            remoteVideoOffer : OfferAnswer?,
            platform : PlatformContext,
            callingViewModel: CallingViewModel,
            videoCallViewModel: VideoCallViewModel,
            navHandler : NavigationHandler,
            modifier: Modifier
        ) {
            LaunchedEffect(Unit) {
                videoCallViewModel.requestPermissionsAndStartVideoCall(platform,
                    onGranted = {
                        videoCallViewModel.startVideoCall(remoteVideoOffer,currentUserId,sessionId,platform)
                    },
                    onDenied = {
                        showToast("Permissions are denied! Return to audio call screen.")
                        navHandler.navigateBack()
                    })
            }
            val localVideoTrackState = videoCallViewModel.localVideoTrackState.collectAsState()
            val remoteVideoTrackState = callingViewModel.remoteVideoTrackState.collectAsState()
            Column(modifier = modifier) {
                WebRTCVideoView(
                    localVideoTrackState.value,
                    remoteVideoTrackState.value,
                    onStopCall = {
                        navHandler.navigateBack()
                    },
                    modifier = Modifier.fillMaxSize()
                )
            }
        }

        fun getScreenName() : String {
            return "VideoCallScreen"
        }
    }
}