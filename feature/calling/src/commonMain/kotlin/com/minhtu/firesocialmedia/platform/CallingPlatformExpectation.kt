package com.minhtu.firesocialmedia.platform

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

expect class WebRTCVideoTrack

@Composable
expect fun WebRTCVideoView(
    localTrack: WebRTCVideoTrack?,
    remoteTrack: WebRTCVideoTrack?,
    isLocalVideoOff: Boolean,
    modifier: Modifier
)
