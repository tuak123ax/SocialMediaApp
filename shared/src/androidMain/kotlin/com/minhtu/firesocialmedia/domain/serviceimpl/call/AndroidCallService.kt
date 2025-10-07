package com.minhtu.firesocialmedia.domain.serviceimpl.call

import android.content.Context
import android.content.Intent
import android.media.AudioManager
import android.util.Log
import com.minhtu.firesocialmedia.constants.Constants
import com.minhtu.firesocialmedia.data.remote.dto.call.IceCandidateDTO
import com.minhtu.firesocialmedia.data.remote.dto.call.OfferAnswerDTO
import com.minhtu.firesocialmedia.data.remote.dto.user.UserDTO
import com.minhtu.firesocialmedia.data.remote.service.call.AudioCallService
import com.minhtu.firesocialmedia.domain.entity.call.CallAction
import com.minhtu.firesocialmedia.platform.WebRTCVideoTrack
import com.minhtu.firesocialmedia.platform.logMessage
import com.minhtu.firesocialmedia.utils.AndroidUtils
import kotlinx.coroutines.withTimeoutOrNull
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.webrtc.AudioSource
import org.webrtc.AudioTrack
import org.webrtc.Camera2Enumerator
import org.webrtc.CameraVideoCapturer
import org.webrtc.DataChannel
import org.webrtc.DefaultVideoDecoderFactory
import org.webrtc.DefaultVideoEncoderFactory
import org.webrtc.EglBase
import org.webrtc.IceCandidate
import org.webrtc.MediaConstraints
import org.webrtc.MediaStream
import org.webrtc.PeerConnection
import org.webrtc.PeerConnectionFactory
import org.webrtc.RtpSender
import org.webrtc.RtpTransceiver
import org.webrtc.SdpObserver
import org.webrtc.SessionDescription
import org.webrtc.SurfaceTextureHelper
import org.webrtc.VideoSource
import org.webrtc.VideoTrack
import org.webrtc.audio.AudioDeviceModule
import org.webrtc.audio.JavaAudioDeviceModule

object WebRTCManager {
    var eglBase: EglBase = EglBase.create()
}
class AndroidAudioCallService(
    context : Context
    ) : AudioCallService {
    private val appContext: Context = context.applicationContext
    private var peerConnectionFactory: PeerConnectionFactory
    private var peerConnection : PeerConnection? = null
    private var isRemoteDescriptionSet: Boolean = false
    private val pendingRemoteIceCandidates: MutableList<IceCandidate> = mutableListOf()
    private var localAudioSource : AudioSource? = null
    private var localAudioTrack : AudioTrack? = null
    private var localVideoSource : VideoSource? = null
    private var localVideoTrack : VideoTrack? = null
    private var remoteVideoTrack: VideoTrack? = null
    private var videoCapturer: CameraVideoCapturer? = null
    private var surfaceTextureHelper : SurfaceTextureHelper? = null
    private var hasStarted = false
    private var localVideoSender: RtpSender? = null
    private lateinit var audioDeviceModule: AudioDeviceModule

    init {
        // 1. Initialize WebRTC global settings
        val options = PeerConnectionFactory.InitializationOptions.builder(appContext)
            .setEnableInternalTracer(true)
            .createInitializationOptions()
        PeerConnectionFactory.initialize(options)

        // 2. Audio device module (for microphone and speaker access)
        audioDeviceModule = JavaAudioDeviceModule.builder(appContext)
            .setUseHardwareAcousticEchoCanceler(true)
            .setUseHardwareNoiseSuppressor(true)
            .createAudioDeviceModule()

        // 3. Create PeerConnectionFactory
        val factoryBuilder = PeerConnectionFactory.builder()
            .setAudioDeviceModule(audioDeviceModule)
            .setVideoEncoderFactory(DefaultVideoEncoderFactory(
                WebRTCManager.eglBase.eglBaseContext,
                /* enableIntelVp8Encoder */ true,
                /* enableH264HighProfile */ true
            ))
            .setVideoDecoderFactory(DefaultVideoDecoderFactory(WebRTCManager.eglBase.eglBaseContext))

        peerConnectionFactory = factoryBuilder.createPeerConnectionFactory()
    }

    /**
     * This function is used to create audio offer for caller.
     * @Param:
     * onOfferCreated: return created audio offer to process next step.
     * */
    override suspend fun createOffer(onOfferCreated : (offer : OfferAnswerDTO) -> Unit) {
        logMessage("createOffer", { "start createOffer" })
        //Create constraints for audio call.
        val constraints = MediaConstraints().apply {
            mandatory.add(MediaConstraints.KeyValuePair("OfferToReceiveAudio", "true"))
        }

        peerConnection?.createOffer(object : SdpObserver{
            override fun onCreateSuccess(description: SessionDescription) {
                //Set local description when create offer success
                peerConnection?.setLocalDescription(object : SdpObserver{
                    override fun onCreateSuccess(p0: SessionDescription?) {
                        logMessage("setLocalDescription", { "onCreateSuccess" })
                    }

                    override fun onSetSuccess() {
                        logMessage("setLocalDescription", { "onSetSuccess" })
                        //Return offer after set to local description.
                        onOfferCreated(OfferAnswerDTO(description.description, description.type.canonicalForm()))
                    }

                    override fun onCreateFailure(p0: String?) {
                        logMessage("setLocalDescription", { "onCreateFailure" })
                    }

                    override fun onSetFailure(p0: String?) {
                        logMessage("setLocalDescription", { "onSetFailure" })
                    }

                }, description)
            }

            override fun onSetSuccess() {
                logMessage("createOffer", { "onSetSuccess" })
            }

            override fun onCreateFailure(p0: String?) {
                logMessage("createOffer", { "onCreateFailure" })
            }

            override fun onSetFailure(p0: String?) {
                logMessage("createOffer", { "onSetFailure" })
            }

        }, constraints)
    }

    /**
     * This function is used to create video offer for caller.
     * @Param:
     * onOfferCreated: return created video offer to process next step.
     * */
    override suspend fun createVideoOffer(
        onOfferCreated: (OfferAnswerDTO) -> Unit) {
        //Create constraints for video call.
        val constraints = MediaConstraints().apply {
            mandatory.add(MediaConstraints.KeyValuePair("OfferToReceiveAudio", "true"))
            mandatory.add(MediaConstraints.KeyValuePair("OfferToReceiveVideo", "true"))
        }

        peerConnection?.createOffer(object : SdpObserver{
            override fun onCreateSuccess(description: SessionDescription) {
                //Set local description when create offer success
                peerConnection?.setLocalDescription(object : SdpObserver{
                    override fun onCreateSuccess(p0: SessionDescription?) {
                        logMessage("setLocalDescription", { "onCreateSuccess" })
                    }

                    override fun onSetSuccess() {
                        logMessage("setLocalDescription", { "onSetSuccess" })
                        //Return offer after set to local description.
                        onOfferCreated(OfferAnswerDTO(description.description, description.type.canonicalForm()))
                    }

                    override fun onCreateFailure(p0: String?) {
                        logMessage("setLocalDescription", { "onCreateFailure" })
                    }

                    override fun onSetFailure(p0: String?) {
                        logMessage("setLocalDescription", { "onSetFailure" })
                    }

                }, description)
            }

            override fun onSetSuccess() {
                logMessage("createOffer", { "onSetSuccess" })
            }

            override fun onCreateFailure(p0: String?) {
                logMessage("createOffer", { "onCreateFailure" })
            }

            override fun onSetFailure(p0: String?) {
                logMessage("createOffer", { "onSetFailure" })
            }

        }, constraints)
    }

    /**
     * This function is used to create answer for callee.
     * @Param:
     * videoSupport: flag to know to create answer for audio call or video call.
     * onAnswerCreated: return created answer to process next step.
     * */
    override suspend fun createAnswer(videoSupport : Boolean,
                              onAnswerCreated : (answer : OfferAnswerDTO) -> Unit) {
        logMessage("createAnswer", { "start createAnswer" })
        //Create constraints for audio or video call.
        var constraints = MediaConstraints()
        constraints = if(videoSupport) {
            MediaConstraints().apply {
                mandatory.add(MediaConstraints.KeyValuePair("OfferToReceiveAudio", "true"))
                mandatory.add(MediaConstraints.KeyValuePair("OfferToReceiveVideo", "true"))
            }
        } else {
            MediaConstraints().apply {
                mandatory.add(MediaConstraints.KeyValuePair("OfferToReceiveAudio", "true"))
            }
        }
        peerConnection?.createAnswer(object : SdpObserver {
            override fun onCreateSuccess(sdp: SessionDescription) {
                //Set local description when create answer success
                logMessage("createAnswer", { "onCreateSuccess" })
                peerConnection?.setLocalDescription(object : SdpObserver{
                    override fun onCreateSuccess(p0: SessionDescription?) {
                        logMessage("setLocalDescription", { "onCreateSuccess" })
                    }

                    override fun onSetSuccess() {
                        logMessage("setLocalDescription", { "onSetSuccess" })
                        //Return answer after set to local description.
                        //Send this answer back to the caller
                        onAnswerCreated(OfferAnswerDTO(sdp.description, sdp.type.canonicalForm()))
                    }

                    override fun onCreateFailure(p0: String?) {
                        logMessage("setLocalDescription", { "onCreateFailure" })
                    }

                    override fun onSetFailure(p0: String?) {
                        logMessage("setLocalDescription", { "onCreateFailure" })
                    }

                },sdp)
            }

            override fun onSetSuccess() {
                logMessage("createAnswer", { "onSetSuccess" })
            }
            override fun onCreateFailure(msg: String?) {
                logMessage("createAnswer", { "onCreateFailure" })
            }
            override fun onSetFailure(msg: String?) {
                logMessage("createAnswer", { "onSetFailure" })
            }
        }, constraints)
    }

    /**
     * This function is used to set remote description when receive from other user.
     * @Param:
     * remoteOffer: remote offer/answer to set in remote description of peer connection.
     * */
    override suspend fun setRemoteDescription(remoteOfferAnswer : OfferAnswerDTO) {
        val sessionDescription = SessionDescription(SessionDescription.Type.fromCanonicalForm(remoteOfferAnswer.type), remoteOfferAnswer.sdp)
        peerConnection?.setRemoteDescription(object : SdpObserver{
            override fun onCreateSuccess(p0: SessionDescription?) {
                logMessage("setRemoteDescription", { "onCreateSuccess" })
            }

            override fun onSetSuccess() {
                logMessage("setRemoteDescription", { "onSetSuccess" })
                isRemoteDescriptionSet = true
                val pendingCount = pendingRemoteIceCandidates.size
                if (pendingCount > 0) {
                    Log.d("WebRTC", "Flushing $pendingCount pending remote ICE candidates")
                    pendingRemoteIceCandidates.forEach { candidate ->
                        val added = peerConnection?.addIceCandidate(candidate) ?: false
                        Log.d("WebRTC", "Flushed ICE candidate added=$added: ${candidate.sdpMid}:${candidate.sdpMLineIndex}")
                    }
                    pendingRemoteIceCandidates.clear()
                }
            }

            override fun onCreateFailure(p0: String?) {
                logMessage("setRemoteDescription", { "onCreateFailure" })
            }

            override fun onSetFailure(p0: String?) {
                logMessage("setRemoteDescription", { "onSetFailure" })
            }

        }, sessionDescription)
    }

    /**
     * This function is called when user accept call from app instead of notification.
     * @Param:
     * sessionId: session id of the call.
     * calleeId: id of callee.
     * */
    override suspend fun acceptCallFromApp(sessionId: String, calleeId: String?) {
        val acceptIntent = Intent(appContext, CallActionBroadcastReceiver::class.java).apply {
            action = CallAction.ACCEPT_CALL_ACTION
            putExtra(Constants.KEY_SESSION_ID, sessionId)
            putExtra(Constants.KEY_CALLEE_ID, calleeId)
            putExtra(Constants.FROM_NOTIFICATION, false)
        }

        appContext.sendBroadcast(acceptIntent)
    }

    override suspend fun callerEndCallFromApp(currentUser : String) {
        val acceptIntent = Intent(appContext, CallActionBroadcastReceiver::class.java).apply {
            action = CallAction.STOP_CALL_ACTION_FROM_CALLER
            putExtra(Constants.FROM_NOTIFICATION, false)
            putExtra(Constants.KEY_CALLER_ID, currentUser)
        }

        appContext.sendBroadcast(acceptIntent)
    }

    override suspend fun calleeEndCallFromApp(sessionId: String, currentUser : String) {
        val acceptIntent = Intent(appContext, CallActionBroadcastReceiver::class.java).apply {
            action = CallAction.STOP_CALL_ACTION_FROM_CALLEE
            putExtra(Constants.KEY_SESSION_ID, sessionId)
            putExtra(Constants.FROM_NOTIFICATION, false)
            putExtra(Constants.KEY_CALLEE_ID, currentUser)
        }

        appContext.sendBroadcast(acceptIntent)
    }

    /**
     * This function is used to add ice candidate to peer connection.
     * */
    override suspend fun addIceCandidate(
        sdp: String,
        sdpMid: String,
        sdpMLineIndex: Int
    ) {
        logMessage("addIceCandidate", { "addIceCandidate" })
        val candidate = IceCandidate(sdpMid, sdpMLineIndex, sdp)
        Log.d("WebRTC", "addIceCandidate called. isRemoteDescriptionSet=$isRemoteDescriptionSet")
        if (isRemoteDescriptionSet) {
            val added = peerConnection?.addIceCandidate(candidate) ?: false
            Log.d("WebRTC", "Applied ICE candidate added=$added: ${candidate.sdpMid}:${candidate.sdpMLineIndex}")
        } else {
            pendingRemoteIceCandidates.add(candidate)
            Log.d("WebRTC", "Queued ICE candidate. Pending size=${pendingRemoteIceCandidates.size}")
        }
    }

    /**
     * This function is used to setup servers, peer connection and audio.
     * @Param:
     * onIceCandidateCreated: return ice candidate when it is initialized.
     * onRemoteVideoTrackReceived: return remote video track to show on screen.
     * */
    override suspend fun initialize(
        onInitializeFinished : () -> Unit,
        onIceCandidateCreated : (iceCandidateData : IceCandidateDTO) -> Unit,
        onRemoteVideoTrackReceived: (remoteVideoTrack :WebRTCVideoTrack) -> Unit) {
        logMessage("initialize", { "start initialize" })
        isRemoteDescriptionSet = false
        pendingRemoteIceCandidates.clear()
        //Setup audio manager
        setupAudioManager()
        //Setup ice servers.
        val iceServers = listOf(
            PeerConnection.IceServer.builder("stun:stun1.l.google.com:19302").createIceServer(),
            PeerConnection.IceServer.builder("stun:stun2.l.google.com:19302").createIceServer(),
            PeerConnection.IceServer.builder("turn:openrelay.metered.ca:80")
                .setUsername("openrelayproject")
                .setPassword("openrelayproject")
                .createIceServer()
        )
        val rtcConfig = PeerConnection.RTCConfiguration(iceServers)
        val observer = object : PeerConnection.Observer{
            override fun onSignalingChange(p0: PeerConnection.SignalingState?) {
                logMessage("initialize", { "onSignalingChange" })
            }

            override fun onIceConnectionChange(p0: PeerConnection.IceConnectionState?) {
                logMessage("initialize", { "onIceConnectionChange" })
                Log.d("WebRTC", "ICE connection state changed: $p0")
            }

            override fun onIceConnectionReceivingChange(p0: Boolean) {
                logMessage("initialize", { "onIceConnectionReceivingChange" })
            }

            override fun onIceGatheringChange(p0: PeerConnection.IceGatheringState?) {
                logMessage("initialize", { "onIceGatheringChange" })
            }

            override fun onIceCandidate(candidate: IceCandidate) {
                logMessage("initialize", { "onIceCandidate" })
                val iceCandidateData = IceCandidateDTO(candidate.sdp, candidate.sdpMid, candidate.sdpMLineIndex)
                onIceCandidateCreated(iceCandidateData)
            }

            override fun onIceCandidatesRemoved(p0: Array<out IceCandidate?>?) {
                logMessage("initialize", { "onIceCandidatesRemoved" })
            }

            override fun onAddStream(p0: MediaStream?) {
                logMessage("initialize", { "onAddStream" })
            }

            override fun onRemoveStream(p0: MediaStream?) {
                logMessage("initialize", { "onRemoveStream" })
            }

            override fun onDataChannel(p0: DataChannel?) {
                logMessage("initialize", { "onDataChannel" })
            }

            override fun onRenegotiationNeeded() {
                logMessage("initialize", { "onRenegotiationNeeded" })
            }

            override fun onTrack(transceiver: RtpTransceiver?) {
                logMessage("initialize", { "onTrack" })
                val receiver = transceiver?.receiver
                val mediaStreamTrack = receiver?.track()
                Log.d("WebRTC", "Track received: ${mediaStreamTrack?.kind()}")
                //Check if the track is audio or video
                when (mediaStreamTrack) {
                    is VideoTrack -> {
                        remoteVideoTrack = mediaStreamTrack
                        onRemoteVideoTrackReceived(WebRTCVideoTrack(remoteVideoTrack))
                    }
                    is AudioTrack -> {
                        mediaStreamTrack.setEnabled(true) // Ensure audio plays
                        Log.d("WebRTC", "Remote audio track enabled: ${mediaStreamTrack.enabled()}")
                    }
                }
            }
        }
        //Setup peer connection.
        peerConnection = peerConnectionFactory.createPeerConnection(rtcConfig,observer)
        //Setup audio track.
        setupAudioTrack()
        onInitializeFinished()
        logMessage("initialize", { "onInitializeFinished" })
    }

    /**
     * This function is used to setup audio manager.
     * */
    private fun setupAudioManager() {
        val audioManager = appContext.getSystemService(Context.AUDIO_SERVICE) as AudioManager
        audioManager.mode = AudioManager.MODE_IN_COMMUNICATION
        audioManager.isSpeakerphoneOn = true
    }

    /**
     * This function is used to start video call.
     * @Param:
     * onStartVideoCall: return local video track when it is available.
     * */
    override suspend fun startVideoCall(
        onStartVideoCall: suspend (videoTrack: WebRTCVideoTrack) -> Unit
    ) {
        if (hasStarted) {
            Log.w("WebRTC", "startVideoCall already called.")
            return
        }

        hasStarted = true

        if (videoCapturer == null) {
            videoCapturer = createCameraCapturer()
        }

        // Ensure EGL context exists
        if (WebRTCManager.eglBase.eglBaseContext == null) {
            try {
                WebRTCManager.eglBase = EglBase.create()
            } catch (e: Exception) {
                Log.e("WebRTC", "Failed to create EglBase: ${e.message}")
                hasStarted = false
                return
            }
        }

        val eglContext = WebRTCManager.eglBase.eglBaseContext ?: run {
            Log.e("WebRTC", "EGL context is invalid.")
            hasStarted = false
            return
        }

        // Release previous SurfaceTextureHelper if needed
        surfaceTextureHelper?.dispose()
        surfaceTextureHelper = try {
            SurfaceTextureHelper.create("CaptureThread", eglContext)
        } catch (e: Exception) {
            Log.e("WebRTC", "SurfaceTextureHelper creation failed: ${e.message}")
            hasStarted = false
            return
        }

        localVideoSource = peerConnectionFactory.createVideoSource(videoCapturer!!.isScreencast)

        try {
            videoCapturer?.initialize(surfaceTextureHelper, appContext, localVideoSource?.capturerObserver)
            videoCapturer?.startCapture(720, 1280, 30)
        } catch (e: Exception) {
            Log.e("WebRTC", "Capturer start failed: ${e.message}")
            surfaceTextureHelper?.dispose()
            hasStarted = false
            return
        }

        localVideoTrack = peerConnectionFactory.createVideoTrack("video", localVideoSource)
        localVideoTrack?.setEnabled(true)

        // Remove previous video sender if exists
        localVideoSender?.let { sender ->
            peerConnection?.removeTrack(sender)
        }

        // Add the video track and keep reference to sender
        localVideoSender = peerConnection?.addTrack(localVideoTrack)

        onStartVideoCall(WebRTCVideoTrack(localVideoTrack))
    }

    /**
     * This function is used to create camera capturer.
     * */
    fun createCameraCapturer() : CameraVideoCapturer? {
        val enumerator = Camera2Enumerator(appContext)
        val devicesName = enumerator.deviceNames
        //Find front camera
        for(deviceName in devicesName) {
            if(enumerator.isFrontFacing(deviceName)) {
                val capturer = enumerator.createCapturer(deviceName, null)
                if(capturer != null) return capturer
            }
        }

        //Fallback to any camera
        for(deviceName in devicesName) {
            if(!enumerator.isFrontFacing(deviceName)) {
                val capturer = enumerator.createCapturer(deviceName, null)
                if(capturer != null) return capturer
            }
        }
        return null
    }

    private var callForegroundServiceIntent : Intent? = null
    /**
     * This function is used to start call foreground service.
     * @Param:
     * sessionId: id of call session.
     * caller: information of caller.
     * callee: information of callee.
     * */
    override suspend fun startCallService(sessionId : String, caller : UserDTO, callee : UserDTO) {
        callForegroundServiceIntent = Intent(appContext, CallForegroundService::class.java)
        callForegroundServiceIntent?.putExtra("sessionId", sessionId)
        callForegroundServiceIntent?.putExtra("caller", Json.encodeToString(caller))
        callForegroundServiceIntent?.putExtra("callee", Json.encodeToString(callee))
        if(callForegroundServiceIntent != null) {
            AndroidUtils.startForegroundService(appContext, callForegroundServiceIntent!!)
        }
    }

    private var videoCallForegroundServiceIntent : Intent? = null
    /**
     * This function is used to start video call foreground service.
     * @Param:
     * sessionId: id of call session.
     * caller: information of caller.
     * callee: information of callee.
     * currentUserId: id of current user.
     * remoteVideoOffer: offer from caller if have.
     * */
    override suspend fun startVideoCallService(sessionId : String,
                                               caller : UserDTO,
                                               callee : UserDTO,
                                               currentUserId : String?,
                                               remoteVideoOffer : OfferAnswerDTO?) {
        videoCallForegroundServiceIntent = Intent(appContext, CallForegroundService::class.java).apply {
            action = CallAction.START_VIDEO_CALL
        }
        videoCallForegroundServiceIntent?.putExtra("sessionId", sessionId)
        videoCallForegroundServiceIntent?.putExtra("caller", Json.encodeToString(caller))
        videoCallForegroundServiceIntent?.putExtra("callee", Json.encodeToString(callee))
        if(remoteVideoOffer != null) {
            videoCallForegroundServiceIntent?.putExtra("remoteVideoOffer", Json.encodeToString(remoteVideoOffer))
        }
        videoCallForegroundServiceIntent?.putExtra("currentUserId", currentUserId)
        if(videoCallForegroundServiceIntent != null) {
            AndroidUtils.startForegroundService(appContext, videoCallForegroundServiceIntent!!)
        }
    }

    /**
     * This function is used to reject video call.
     * */
    override suspend fun rejectVideoCall() {
        videoCallForegroundServiceIntent = Intent(appContext, CallForegroundService::class.java).apply {
            action = CallAction.REJECT_VIDEO_CALL
        }
        if(videoCallForegroundServiceIntent != null) {
            AndroidUtils.startForegroundService(appContext, videoCallForegroundServiceIntent!!)
        }
    }

    /**
     * This function is used to setup audio track.
     * */
    override suspend fun setupAudioTrack() {
        localAudioSource = peerConnectionFactory.createAudioSource(MediaConstraints())
        localAudioTrack = peerConnectionFactory.createAudioTrack("audio", localAudioSource)
        localAudioTrack?.setEnabled(true)
        peerConnection?.addTrack(localAudioTrack)
    }

    /**
     * This function is used to stop call foreground service.
     * */
    override suspend fun stopCall() {
        if(callForegroundServiceIntent != null) {
            appContext.stopService(callForegroundServiceIntent)
        }
    }

    /**
     * This function is used to release all resources of foreground service.
     * */
    override suspend fun releaseResources() {
        try {
            logMessage("CallForegroundService") { "Releasing WebRTC resources" }
            callForegroundServiceIntent = null
            videoCallForegroundServiceIntent = null

            // Stop video capturer first
            videoCapturer?.let { capturer ->
                withTimeoutOrNull(2000) {
                    try { capturer.stopCapture() }
                    catch (e: Exception) { logMessage("CallForegroundService") { "Error stopping video capturer: ${e.message}" } }
                    finally { runCatching { capturer.dispose() } }
                }
            }
            videoCapturer = null

            // Dispose local sources
            localVideoSource?.dispose()
            localVideoSource = null
            localAudioSource?.dispose()
            localAudioSource = null

            surfaceTextureHelper?.stopListening()
            surfaceTextureHelper?.dispose()
            surfaceTextureHelper = null

            localVideoTrack = null
            remoteVideoTrack = null
            localAudioTrack = null
            localVideoSender = null
            hasStarted = false

            // PeerConnection
            peerConnection?.close()
            peerConnection?.dispose()
            peerConnection = null
            logMessage("CallForegroundService") { "Released peer Connection success" }

            // Factory
            peerConnectionFactory.dispose()
            logMessage("CallForegroundService") { "Released peer Connection Factory success" }

            // Stop WebRTC network monitoring
            org.webrtc.NetworkMonitor.getInstance().stopMonitoring()
            logMessage("CallForegroundService") { "Released network monitoring success" }

            // EGL & Tracer
            WebRTCManager.eglBase.release()
            PeerConnectionFactory.shutdownInternalTracer()

            // Reset audio routing and release AudioDeviceModule
            runCatching {
                val am = appContext.getSystemService(Context.AUDIO_SERVICE) as AudioManager
                am.mode = AudioManager.MODE_NORMAL
                am.isSpeakerphoneOn = false
            }
            runCatching { audioDeviceModule.release() }

            logMessage("CallForegroundService") { "WebRTC resources released successfully" }

        } catch (e: Exception) {
            logMessage("CallForegroundService") { "Failed to stop WebRTC cleanly: ${e.message}" }
        }
    }
}