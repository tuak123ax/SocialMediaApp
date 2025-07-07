package com.minhtu.firesocialmedia.services.audiocall

import android.content.Context
import android.media.AudioManager
import android.util.Log
import com.minhtu.firesocialmedia.data.model.call.IceCandidateData
import com.minhtu.firesocialmedia.data.model.call.OfferAnswer
import com.minhtu.firesocialmedia.di.AudioCallService
import com.minhtu.firesocialmedia.platform.WebRTCVideoTrack
import com.minhtu.firesocialmedia.platform.logMessage
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
import org.webrtc.RtpTransceiver
import org.webrtc.SdpObserver
import org.webrtc.SessionDescription
import org.webrtc.SurfaceTextureHelper
import org.webrtc.VideoTrack
import org.webrtc.audio.JavaAudioDeviceModule

class AndroidAudioCallService(
    private val context : Context
    ) : AudioCallService{
    private var eglBase = EglBase.create()
    private var peerConnectionFactory: PeerConnectionFactory
    private var peerConnection : PeerConnection? = null
    private var localAudioSource : AudioSource? = null
    private var localVideoTrack : VideoTrack? = null
    private var remoteVideoTrack: VideoTrack? = null

    init {
        // 1. Initialize WebRTC global settings
        val options = PeerConnectionFactory.InitializationOptions.builder(context)
            .setEnableInternalTracer(true)
            .createInitializationOptions()
        PeerConnectionFactory.initialize(options)

        // 2. Audio device module (for microphone and speaker access)
        val audioDeviceModule = JavaAudioDeviceModule.builder(context)
            .setUseHardwareAcousticEchoCanceler(true)
            .setUseHardwareNoiseSuppressor(true)
            .createAudioDeviceModule()

        // 3. Create PeerConnectionFactory
        val factoryBuilder = PeerConnectionFactory.builder()
            .setAudioDeviceModule(audioDeviceModule)
            .setVideoEncoderFactory(DefaultVideoEncoderFactory(
                eglBase.eglBaseContext,
                /* enableIntelVp8Encoder */ true,
                /* enableH264HighProfile */ true
            ))
            .setVideoDecoderFactory(DefaultVideoDecoderFactory(eglBase.eglBaseContext))

        peerConnectionFactory = factoryBuilder.createPeerConnectionFactory()
    }

    override fun createOffer(onOfferCreated : (offer : OfferAnswer) -> Unit) {
        val constraints = MediaConstraints().apply {
            mandatory.add(MediaConstraints.KeyValuePair("OfferToReceiveAudio", "true"))
        }

        peerConnection?.createOffer(object : SdpObserver{
            override fun onCreateSuccess(description: SessionDescription) {
                peerConnection?.setLocalDescription(object : SdpObserver{
                    override fun onCreateSuccess(p0: SessionDescription?) {
                        logMessage("setLocalDescription", "onCreateSuccess")
                    }

                    override fun onSetSuccess() {
                        logMessage("setLocalDescription", "onSetSuccess")
                        onOfferCreated(OfferAnswer(description.description, description.type.canonicalForm()))
                    }

                    override fun onCreateFailure(p0: String?) {
                        logMessage("setLocalDescription", "onCreateFailure")
                    }

                    override fun onSetFailure(p0: String?) {
                        logMessage("setLocalDescription", "onSetFailure")
                    }

                }, description)
            }

            override fun onSetSuccess() {
                logMessage("createOffer", "onSetSuccess")
            }

            override fun onCreateFailure(p0: String?) {
                logMessage("createOffer", "onCreateFailure")
            }

            override fun onSetFailure(p0: String?) {
                logMessage("createOffer", "onSetFailure")
            }

        }, constraints)
    }

    override fun createVideoOffer(onOfferCreated: (OfferAnswer) -> Unit) {
        val constraints = MediaConstraints().apply {
            mandatory.add(MediaConstraints.KeyValuePair("OfferToReceiveAudio", "true"))
            mandatory.add(MediaConstraints.KeyValuePair("OfferToReceiveVideo", "true"))
        }

        peerConnection?.createOffer(object : SdpObserver{
            override fun onCreateSuccess(description: SessionDescription) {
                peerConnection?.setLocalDescription(object : SdpObserver{
                    override fun onCreateSuccess(p0: SessionDescription?) {
                        logMessage("setLocalDescription", "onCreateSuccess")
                    }

                    override fun onSetSuccess() {
                        logMessage("setLocalDescription", "onSetSuccess")
                        onOfferCreated(OfferAnswer(description.description, description.type.canonicalForm()))
                    }

                    override fun onCreateFailure(p0: String?) {
                        logMessage("setLocalDescription", "onCreateFailure")
                    }

                    override fun onSetFailure(p0: String?) {
                        logMessage("setLocalDescription", "onSetFailure")
                    }

                }, description)
            }

            override fun onSetSuccess() {
                logMessage("createOffer", "onSetSuccess")
            }

            override fun onCreateFailure(p0: String?) {
                logMessage("createOffer", "onCreateFailure")
            }

            override fun onSetFailure(p0: String?) {
                logMessage("createOffer", "onSetFailure")
            }

        }, constraints)
    }

    override fun createAnswer(videoSupport : Boolean,
                              onAnswerCreated : (answer : OfferAnswer) -> Unit) {
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
                logMessage("createAnswer", "onCreateSuccess")
                peerConnection?.setLocalDescription(object : SdpObserver{
                    override fun onCreateSuccess(p0: SessionDescription?) {
                        logMessage("setLocalDescription", "onCreateSuccess")
                    }

                    override fun onSetSuccess() {
                        logMessage("setLocalDescription", "onSetSuccess")
                        // Send this answer back to the caller
                        onAnswerCreated(OfferAnswer(sdp.description, sdp.type.canonicalForm()))
                    }

                    override fun onCreateFailure(p0: String?) {
                        logMessage("setLocalDescription", "onCreateFailure")
                    }

                    override fun onSetFailure(p0: String?) {
                        logMessage("setLocalDescription", "onCreateFailure")
                    }

                },sdp)
            }

            override fun onSetSuccess() {
                logMessage("createAnswer", "onSetSuccess")
            }
            override fun onCreateFailure(msg: String?) {
                logMessage("createAnswer", "onCreateFailure")
            }
            override fun onSetFailure(msg: String?) {
                logMessage("createAnswer", "onSetFailure")
            }
        }, constraints)
    }

    override fun setRemoteDescription(remoteOffer : OfferAnswer) {
        val sessionDescription = SessionDescription(SessionDescription.Type.fromCanonicalForm(remoteOffer.type), remoteOffer.sdp)
        peerConnection?.setRemoteDescription(object : SdpObserver{
            override fun onCreateSuccess(p0: SessionDescription?) {
                logMessage("setRemoteDescription", "onCreateSuccess")
            }

            override fun onSetSuccess() {
                logMessage("setRemoteDescription", "onSetSuccess")
            }

            override fun onCreateFailure(p0: String?) {
                logMessage("setRemoteDescription", "onCreateFailure")
            }

            override fun onSetFailure(p0: String?) {
                logMessage("setRemoteDescription", "onSetFailure")
            }

        }, sessionDescription)
    }

    override fun addIceCandidate(
        sdp: String,
        sdpMid: String,
        sdpMLineIndex: Int
    ) {
        logMessage("addIceCandidate", "addIceCandidate")
        val candidate = IceCandidate(sdpMid, sdpMLineIndex, sdp)
        peerConnection?.addIceCandidate(candidate)
    }

    override fun initialize(
        onIceCandidateCreated : (iceCandidateData : IceCandidateData) -> Unit,
        onRemoteVideoTrackReceived: (remoteVideoTrack :WebRTCVideoTrack) -> Unit) {
        setupAudioManager()
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
                logMessage("initialize", "onSignalingChange")
            }

            override fun onIceConnectionChange(p0: PeerConnection.IceConnectionState?) {
                logMessage("initialize", "onIceConnectionChange")
                Log.d("WebRTC", "ICE connection state changed: $p0")
            }

            override fun onIceConnectionReceivingChange(p0: Boolean) {
                logMessage("initialize", "onIceConnectionReceivingChange")
            }

            override fun onIceGatheringChange(p0: PeerConnection.IceGatheringState?) {
                logMessage("initialize", "onIceGatheringChange")
            }

            override fun onIceCandidate(candidate: IceCandidate) {
                logMessage("initialize", "onIceCandidate")
                val iceCandidateData = IceCandidateData(candidate.sdp, candidate.sdpMid, candidate.sdpMLineIndex)
                onIceCandidateCreated(iceCandidateData)
            }

            override fun onIceCandidatesRemoved(p0: Array<out IceCandidate?>?) {
                logMessage("initialize", "onIceCandidatesRemoved")
            }

            override fun onAddStream(p0: MediaStream?) {
                logMessage("initialize", "onAddStream")
            }

            override fun onRemoveStream(p0: MediaStream?) {
                logMessage("initialize", "onRemoveStream")
            }

            override fun onDataChannel(p0: DataChannel?) {
                logMessage("initialize", "onDataChannel")
            }

            override fun onRenegotiationNeeded() {
                logMessage("initialize", "onRenegotiationNeeded")
            }

            override fun onTrack(transceiver: RtpTransceiver?) {
                logMessage("initialize", "onTrack")
                val receiver = transceiver?.receiver
                val mediaStreamTrack = receiver?.track()
                Log.d("WebRTC", "Track received: ${mediaStreamTrack?.kind()}")
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
        peerConnection = peerConnectionFactory.createPeerConnection(rtcConfig,observer)
    }

    private fun setupAudioManager() {
        val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
        audioManager.mode = AudioManager.MODE_IN_COMMUNICATION
        audioManager.isSpeakerphoneOn = true
    }

    override fun startVideoCall(onStartVideoCall : (videoTrack : WebRTCVideoTrack) -> Unit) {
        val videoCapturer = createCameraCapturer()
        if(videoCapturer != null) {
            if(eglBase == null) {
                eglBase = EglBase.create()
            }
            val surfaceTextureHelper = SurfaceTextureHelper.create("CaptureThread", eglBase.eglBaseContext)
            val videoSource = peerConnectionFactory.createVideoSource(videoCapturer.isScreencast)
            videoCapturer.initialize(surfaceTextureHelper, context, videoSource.capturerObserver)
            videoCapturer.startCapture(720, 1280, 30)

            localVideoTrack = peerConnectionFactory.createVideoTrack("video", videoSource)
            localVideoTrack?.setEnabled(true)
            peerConnection?.addTrack(localVideoTrack)
            onStartVideoCall(WebRTCVideoTrack(localVideoTrack))
        }
    }

    fun createCameraCapturer() : CameraVideoCapturer? {
        val enumerator = Camera2Enumerator(context)
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

    override fun startCall() {
        localAudioSource = peerConnectionFactory.createAudioSource(MediaConstraints())
        val audioTrack = peerConnectionFactory.createAudioTrack("audio", localAudioSource)
        audioTrack.setEnabled(true)
        peerConnection?.addTrack(audioTrack)
    }



    override fun stopCall() {
        peerConnection?.close()
        localAudioSource?.dispose()
        eglBase.release()
    }
}