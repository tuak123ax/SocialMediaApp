package com.minhtu.firesocialmedia.platform

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import android.util.Log
import org.webrtc.EglBase
import org.webrtc.RendererCommon
import org.webrtc.SurfaceViewRenderer
import org.webrtc.VideoTrack

actual class WebRTCVideoTrack(val track: VideoTrack?)

fun isUsableVideoTrack(track: VideoTrack?): Boolean {
    if (track == null) return false

    return try {
        track.enabled()
    } catch (e: Exception) {
        false
    }
}

@Composable
actual fun WebRTCVideoView(
    localTrack: WebRTCVideoTrack?,
    remoteTrack: WebRTCVideoTrack?,
    isLocalVideoOff : Boolean,
    modifier: Modifier
) {
    Box(modifier = modifier) {
        // Keep a black remote canvas when remote video is absent.
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black)
        )

        remoteTrack?.track?.takeIf { isUsableVideoTrack(it) }?.let { track ->
            RemoteVideoView(
                eglBaseContext = WebRTCManager.eglBase.eglBaseContext,
                videoTrack = track,
                modifier = Modifier.fillMaxSize()
            )
        }

        if(!isLocalVideoOff) {
            localTrack?.track?.takeIf { isUsableVideoTrack(it) }?.let {
                LocalVideoView(
                    eglBaseContext = WebRTCManager.eglBase.eglBaseContext,
                    videoTrack = it,
                    modifier = Modifier
                        .size(150.dp)
                        .align(Alignment.BottomEnd)
                        .padding(16.dp)
                )
            }
        }
    }
}

@Composable
fun LocalVideoView(
    eglBaseContext: EglBase.Context,
    videoTrack: VideoTrack,
    modifier: Modifier = Modifier
) {
    var currentRenderer by remember { mutableStateOf<SurfaceViewRenderer?>(null) }

    DisposableEffect(videoTrack, currentRenderer) {
        val renderer = currentRenderer
        if (renderer != null && isUsableVideoTrack(videoTrack)) {
            // Defensive rebind for renegotiation/rejoin: ensure stale bindings are detached first.
            runCatching { videoTrack.removeSink(renderer) }
            runCatching { videoTrack.addSink(renderer) }
                .onFailure { Log.w("LocalVideoView", "Skip addSink on disposed local track", it) }
        }
        onDispose {
            if (renderer != null && isUsableVideoTrack(videoTrack)) {
                runCatching { videoTrack.removeSink(renderer) }
                    .onFailure { Log.e("LocalVideoView", "Failed to remove sink", it) }
                renderer.clearImage()
            }
        }
    }

    AndroidView(
        factory = { context ->
            SurfaceViewRenderer(context).apply {
                init(eglBaseContext, null)
                setZOrderMediaOverlay(true)
                setMirror(true)
                setScalingType(RendererCommon.ScalingType.SCALE_ASPECT_FIT)
                // Avoid SurfaceFlinger buffer-size rejection on dynamic Compose layouts.
                setEnableHardwareScaler(false)
                currentRenderer = this
            }
        },
        onRelease = { renderer ->
            currentRenderer = null
            if (isUsableVideoTrack(videoTrack)) {
                runCatching {
                    try {
                        videoTrack.removeSink(renderer)
                    } catch (_: Exception) {}
                }
            }
            renderer.clearImage()
            renderer.release()
        },
        modifier = modifier
    )
}


@Composable
fun RemoteVideoView(
    eglBaseContext: EglBase.Context,
    videoTrack: VideoTrack,
    modifier: Modifier = Modifier
) {
    var currentRenderer by remember { mutableStateOf<SurfaceViewRenderer?>(null) }

    DisposableEffect(videoTrack, currentRenderer) {
        val renderer = currentRenderer
        if (renderer != null) {
            // Defensive rebind: if this renderer was previously attached to another
            // wrapper, detach first so renegotiation can rebind cleanly.
            runCatching { videoTrack.removeSink(renderer) }
            runCatching { videoTrack.addSink(renderer) }
                .onFailure { Log.w("RemoteVideoView", "Failed to add remote sink", it) }
        }
        onDispose {
            if (renderer != null) {
                runCatching { videoTrack.removeSink(renderer) }
                    .onFailure { Log.e("RemoteVideoView", "Failed to remove sink", it) }
                renderer.clearImage()
            }
        }
    }

    AndroidView(
        factory = { context ->
            SurfaceViewRenderer(context).apply {
                init(eglBaseContext, null)
                setMirror(false)
                setScalingType(RendererCommon.ScalingType.SCALE_ASPECT_FIT)
                // Avoid SurfaceFlinger buffer-size rejection on dynamic Compose layouts.
                setEnableHardwareScaler(false)
                currentRenderer = this
            }
        },
        onRelease = { renderer ->
            currentRenderer = null
            runCatching {
                try {
                    videoTrack.removeSink(renderer)
                } catch (_: Exception) {}
            }
            renderer.clearImage()
            renderer.release()
        },
        modifier = modifier
    )
}
