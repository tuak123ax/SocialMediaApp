package com.minhtu.firesocialmedia.platform

import androidx.compose.foundation.layout.Box
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

actual class WebRTCVideoTrack

@Composable
actual fun WebRTCVideoView(
    localTrack: WebRTCVideoTrack?,
    remoteTrack: WebRTCVideoTrack?,
    isLocalVideoOff : Boolean,
    modifier: Modifier
) {
    // iOS implementation will be added later
    Box(modifier = modifier) {
        Text("WebRTC Video View - iOS implementation coming soon")
    }
}
