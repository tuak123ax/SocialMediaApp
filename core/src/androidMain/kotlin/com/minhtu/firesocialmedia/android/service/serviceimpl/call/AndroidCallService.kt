package com.minhtu.firesocialmedia.android.service.serviceimpl.call

import android.content.Context
import android.content.Intent
import android.media.AudioManager
import android.util.Log
import com.minhtu.firesocialmedia.core.constants.Constants
import com.minhtu.firesocialmedia.data.remote.dto.call.IceCandidateDTO
import com.minhtu.firesocialmedia.data.remote.dto.call.OfferAnswerDTO
import com.minhtu.firesocialmedia.data.remote.dto.user.UserDTO
import com.minhtu.firesocialmedia.data.remote.service.call.AudioCallService
import com.minhtu.firesocialmedia.core.domain.entity.call.CallAction
import com.minhtu.firesocialmedia.core.domain.entity.call.CallEventFlow
import com.minhtu.firesocialmedia.core.domain.entity.call.SpeakerType
import com.minhtu.firesocialmedia.platform.WebRTCVideoTrack
import com.minhtu.firesocialmedia.platform.logMessage
import com.minhtu.firesocialmedia.utils.AndroidUtils
import kotlinx.coroutines.suspendCancellableCoroutine
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
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

object WebRTCManager {
    var eglBase: EglBase = EglBase.create()
}

/**
 * Singleton so that both CallForegroundService (which runs the call and holds localVideoTrack)
 * and the VideoCall UI (which calls updateCameraStatus) use the same instance. Otherwise
 * updateCameraStatus would run on an instance with null localVideoTrack and have no effect.
 */
class AndroidAudioCallService private constructor(
    context : Context
) : AudioCallService {
    private val appContext: Context = context.applicationContext

    companion object {
        @Volatile
        private var instance: AndroidAudioCallService? = null

        fun get(context: Context): AndroidAudioCallService {
            return instance ?: synchronized(this) {
                instance ?: AndroidAudioCallService(context.applicationContext).also { instance = it }
            }
        }

        /** Clear singleton after releasing resources so the next call gets a fresh instance. */
        fun clearInstance() {
            synchronized(this) {
                instance = null
            }
        }
    }
    private var peerConnectionFactory: PeerConnectionFactory
    private var peerConnection : PeerConnection? = null
    private var isRemoteDescriptionSet: Boolean = false
    private val pendingRemoteIceCandidates: MutableList<IceCandidate> = mutableListOf()
    private var localAudioSource : AudioSource? = null
    private var localAudioTrack : AudioTrack? = null
    private var localVideoSource : VideoSource? = null
    private var localVideoTrack : VideoTrack? = null
    private var remoteAudioTrack: AudioTrack? = null
    private var remoteVideoTrack: VideoTrack? = null
    private var videoCapturer: CameraVideoCapturer? = null
    private var surfaceTextureHelper : SurfaceTextureHelper? = null
    private var hasStarted = false
    private var localVideoSender: RtpSender? = null
    private var audioDeviceModule: AudioDeviceModule
    private var peerConnectionObserver : PeerConnection.Observer? = null
    private var lastSpeakerType: SpeakerType = SpeakerType.Audio
    private var onRemoteVideoTrackReceivedCallback: ((WebRTCVideoTrack) -> Unit)? = null

    init {
        // 0. Fresh EglBase for this instance (previous one was released in releaseResources())
        WebRTCManager.eglBase = EglBase.create()

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
        val constraints = MediaConstraints()

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
                        logMessage("setLocalDescription", { "onSetFailure: ${p0 ?: "unknown"}" })
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
        // A video upgrade starts a new negotiation. Queue any remote ICE that arrives
        // until the upgraded remote answer is applied.
        isRemoteDescriptionSet = false
        pendingRemoteIceCandidates.clear()
        //Create constraints for video call.
        val constraints = MediaConstraints().apply {
            mandatory.add(MediaConstraints.KeyValuePair("OfferToReceiveAudio", "true"))
            mandatory.add(MediaConstraints.KeyValuePair("OfferToReceiveVideo", "true"))
        }

        peerConnection?.createOffer(object : SdpObserver{
            override fun onCreateSuccess(description: SessionDescription) {
                val normalizedDescription = normalizeVideoSdp(description)
                //Set local description when create offer success
                peerConnection?.setLocalDescription(object : SdpObserver{
                    override fun onCreateSuccess(p0: SessionDescription?) {
                        logMessage("setLocalDescription", { "onCreateSuccess" })
                    }

                    override fun onSetSuccess() {
                        logMessage("setLocalDescription", { "onSetSuccess" })
                        //Return offer after set to local description.
                        onOfferCreated(
                            OfferAnswerDTO(
                                normalizedDescription.description,
                                normalizedDescription.type.canonicalForm()
                            )
                        )
                    }

                    override fun onCreateFailure(p0: String?) {
                        logMessage("setLocalDescription", { "onCreateFailure" })
                    }

                    override fun onSetFailure(p0: String?) {
                        logMessage("setLocalDescription", { "onSetFailure: ${p0 ?: "unknown"}" })
                    }

                }, normalizedDescription)
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
        val constraints = MediaConstraints().apply {
            mandatory.add(MediaConstraints.KeyValuePair("OfferToReceiveAudio", "true"))
            mandatory.add(MediaConstraints.KeyValuePair("OfferToReceiveVideo", "true"))
        }
        peerConnection?.createAnswer(object : SdpObserver {
            override fun onCreateSuccess(sdp: SessionDescription) {
                val normalizedDescription = if (videoSupport) normalizeVideoSdp(sdp) else sdp
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
                        onAnswerCreated(
                            OfferAnswerDTO(
                                normalizedDescription.description,
                                normalizedDescription.type.canonicalForm()
                            )
                        )
                    }

                    override fun onCreateFailure(p0: String?) {
                        logMessage("setLocalDescription", { "onCreateFailure" })
                    }

                    override fun onSetFailure(p0: String?) {
                        logMessage("setLocalDescription", { "onSetFailure: ${p0 ?: "unknown"}" })
                    }

                }, normalizedDescription)
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
        Log.d(
            "WebRTC",
            "setRemoteDescription called: type=${remoteOfferAnswer.type}, sdpLength=${remoteOfferAnswer.sdp?.length}, signalingState=${peerConnection?.signalingState()}"
        )
        val peerConnection = peerConnection
            ?: throw IllegalStateException("PeerConnection is null when setting remote description")
        val type = remoteOfferAnswer.type
            ?: throw IllegalArgumentException("Remote description type is null")
        val sdp = remoteOfferAnswer.sdp
            ?: throw IllegalArgumentException("Remote description SDP is null")
        val sessionDescription = SessionDescription(
            SessionDescription.Type.fromCanonicalForm(type),
            sdp
        )
        // During renegotiation, queue any incoming ICE until this SDP is fully applied.
        isRemoteDescriptionSet = false

        suspendCancellableCoroutine<Unit> { continuation ->
            peerConnection.setRemoteDescription(object : SdpObserver{
                override fun onCreateSuccess(p0: SessionDescription?) {
                    logMessage("setRemoteDescription", { "onCreateSuccess" })
                }

                override fun onSetSuccess() {
                    Log.d("WebRTC", "setRemoteDescription onSetSuccess: signalingState=${this@AndroidAudioCallService.peerConnection?.signalingState()}")
                    isRemoteDescriptionSet = true
                    val pendingCount = pendingRemoteIceCandidates.size
                    if (pendingCount > 0) {
                        Log.d("WebRTC", "Flushing $pendingCount pending remote ICE candidates")
                        pendingRemoteIceCandidates.forEach { candidate ->
                            val added = this@AndroidAudioCallService.peerConnection?.addIceCandidate(candidate) ?: false
                            Log.d("WebRTC", "Flushed ICE candidate added=$added: ${candidate.sdpMid}:${candidate.sdpMLineIndex}")
                        }
                        pendingRemoteIceCandidates.clear()
                    }
                    checkForRemoteVideoTrack("remote-description-set")
                    if (continuation.isActive) continuation.resume(Unit)
                }

                override fun onCreateFailure(p0: String?) {
                    val message = "setRemoteDescription onCreateFailure: $p0"
                    Log.e("WebRTC", message)
                    logMessage("setRemoteDescription", { message })
                    if (continuation.isActive) {
                        continuation.resumeWithException(IllegalStateException(message))
                    }
                }

                override fun onSetFailure(p0: String?) {
                    val message = "setRemoteDescription onSetFailure: $p0"
                    if (
                        sessionDescription.type == SessionDescription.Type.ANSWER &&
                        p0?.contains("Called in wrong state: stable") == true
                    ) {
                        Log.w("WebRTC", "Ignoring duplicate remote answer after negotiation completed")
                        if (continuation.isActive) continuation.resume(Unit)
                        return
                    }
                    Log.e("WebRTC", message)
                    logMessage("setRemoteDescription", { message })
                    if (continuation.isActive) {
                        continuation.resumeWithException(IllegalStateException(message))
                    }
                }

            }, sessionDescription)
        }
    }

    /**
     * Explicitly check for remote video tracks after setRemoteDescription succeeds.
     * onTrack may not fire during renegotiation when the transceiver was already created
     * locally via addTrack, so we scan transceivers as a fallback.
     */
    private fun isVideoTrackUsable(track: VideoTrack?): Boolean {
        if (track == null) return false
        return runCatching {
            track.enabled()
            true
        }.getOrElse { false }
    }

    private fun isVideoSenderUsable(sender: RtpSender?): Boolean {
        if (sender == null) return false
        return runCatching {
            val senderTrack = sender.track() as? VideoTrack ?: return false
            isVideoTrackUsable(senderTrack)
        }.getOrElse { false }
    }

    private fun findReusableLocalVideoSendPath(): Pair<RtpSender, VideoTrack>? {
        val preferredSender = localVideoSender
        val preferredTrack = runCatching { preferredSender?.track() as? VideoTrack }.getOrNull()
        if (preferredSender != null && isVideoTrackUsable(preferredTrack)) {
            return preferredSender to preferredTrack!!
        }

        val transceiverSender = runCatching {
            peerConnection?.transceivers
                ?.mapNotNull { transceiver ->
                    val sender = runCatching { transceiver.sender }.getOrNull() ?: return@mapNotNull null
                    val senderTrack = runCatching { sender.track() as? VideoTrack }.getOrNull() ?: return@mapNotNull null
                    if (isVideoTrackUsable(senderTrack)) sender to senderTrack else null
                }
                ?.firstOrNull()
        }.getOrNull()

        if (transceiverSender != null) {
            localVideoSender = transceiverSender.first
        }
        return transceiverSender
    }

    private suspend fun emitLocalVideoTrack(
        track: VideoTrack,
        onStartVideoCall: suspend (videoTrack: WebRTCVideoTrack) -> Unit
    ) {
        val wrapped = WebRTCVideoTrack(track)
        onStartVideoCall(wrapped)
        // Safety net: keep local track flow in sync even if upper callback scope is cancelled.
        CallEventFlow.localVideoTrack.value = wrapped
    }

    private fun checkForRemoteVideoTrack(reason: String = "unspecified") {
        val transceivers = try {
            peerConnection?.transceivers
        } catch (e: Exception) {
            Log.e("WebRTC", "checkForRemoteVideoTrack: failed to get transceivers", e)
            null
        }
        val currentTrack = remoteVideoTrack
        val hasUsableCurrentTrack = isVideoTrackUsable(currentTrack)
        Log.d(
            "WebRTC",
            "checkForRemoteVideoTrack[$reason]: transceiverCount=${transceivers?.size}, currentRemoteVideoTrack=$currentTrack, currentTrackUsable=$hasUsableCurrentTrack, callbackSet=${onRemoteVideoTrackReceivedCallback != null}"
        )
        var candidateTrack: VideoTrack? = null
        transceivers?.forEach { transceiver ->
            val track = runCatching { transceiver.receiver?.track() }.getOrNull()
            val trackUsable = (track as? VideoTrack)?.let { isVideoTrackUsable(it) } ?: false
            Log.d("WebRTC", "  transceiver mid=${transceiver.mid}, direction=${transceiver.direction}, receiverTrack kind=${track?.kind()}, type=${track?.javaClass?.simpleName}, sameAsCurrent=${track === remoteVideoTrack}, usable=$trackUsable")
            if (track is VideoTrack && trackUsable) {
                candidateTrack = track
                return@forEach
            }
        }
        when {
            candidateTrack == null -> {
                if (currentTrack != null) {
                    Log.d(
                        "WebRTC",
                        "checkForRemoteVideoTrack[$reason]: no remote video track found, clearing stale remote track"
                    )
                    remoteVideoTrack = null
                    CallEventFlow.remoteVideoTrack.value = null
                } else {
                    Log.d("WebRTC", "checkForRemoteVideoTrack[$reason]: no usable remote video track found yet")
                }
            }
            candidateTrack !== currentTrack -> {
                if (hasUsableCurrentTrack) {
                    // During renegotiation, WebRTC may hand us a different VideoTrack wrapper
                    // for the same transceiver momentarily. Replacing a working track with
                    // that wrapper can race with disposal and cause addSink failures.
                    Log.d(
                        "WebRTC",
                        "checkForRemoteVideoTrack[$reason]: keeping existing usable remote track; skip wrapper swap"
                    )
                } else {
                    Log.d("WebRTC", "checkForRemoteVideoTrack[$reason]: emitting refreshed remote video track")
                    emitRemoteVideoTrack(candidateTrack)
                }
            }
            !hasUsableCurrentTrack -> {
                Log.d("WebRTC", "checkForRemoteVideoTrack[$reason]: re-emitting current remote video track")
                emitRemoteVideoTrack(candidateTrack)
            }
            else -> {
                if (CallEventFlow.remoteVideoTrack.value == null) {
                    Log.d(
                        "WebRTC",
                        "checkForRemoteVideoTrack[$reason]: current remote track exists but flow is null, re-emitting"
                    )
                    emitRemoteVideoTrack(currentTrack)
                } else {
                    Log.d("WebRTC", "checkForRemoteVideoTrack[$reason]: keeping existing remote video track")
                }
            }
        }
    }

    /**
     * Central helper to emit a newly detected remote video track.
     * Uses both the callback chain AND directly sets CallEventFlow as a safety net
     * (in case the callback's coroutine scope is cancelled).
     */
    private fun emitRemoteVideoTrack(track: VideoTrack) {
        if (!isVideoTrackUsable(track)) {
            Log.w("WebRTC", "emitRemoteVideoTrack: skip disposed/invalid remote track wrapper=${System.identityHashCode(track)}")
            return
        }
        // Ensure rendering is not blocked by a disabled remote track state.
        runCatching { track.setEnabled(true) }
        remoteVideoTrack = track
        val wrapped = WebRTCVideoTrack(track)
        Log.d("WebRTC", "emitRemoteVideoTrack: wrapper=${System.identityHashCode(track)}, id=${runCatching { track.id() }.getOrNull()}")
        onRemoteVideoTrackReceivedCallback?.invoke(wrapped)
        CallEventFlow.remoteVideoTrack.value = wrapped
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
        Log.d("WebRTC", "addIceCandidate called. isRemoteDescriptionSet=$isRemoteDescriptionSet, mLineIndex=$sdpMLineIndex")
        
        // If the remote description is set BUT the candidate is for an m-line we don't have yet 
        // (e.g., video candidate arriving before video offer is applied), we must queue it.
        // Assuming audio is mLineIndex 0 and video is mLineIndex 1.
        val isVideoCandidateArrivingEarly = isRemoteDescriptionSet && sdpMLineIndex > 0 && (peerConnection?.remoteDescription?.description?.contains("m=video") != true)

        if (isRemoteDescriptionSet && !isVideoCandidateArrivingEarly) {
            val added = peerConnection?.addIceCandidate(candidate) ?: false
            Log.d("WebRTC", "Applied ICE candidate added=$added: ${candidate.sdpMid}:${candidate.sdpMLineIndex}")
            if (added) {
                checkForRemoteVideoTrack("ice-candidate-added")
            }
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
        onRemoteVideoTrackReceivedCallback = onRemoteVideoTrackReceived
        //Setup audio manager
        setupAudioManager()
        //Setup ice servers.
        val iceServers = listOf(
            PeerConnection.IceServer.builder("stun:stun1.l.google.com:19302").createIceServer(),
            PeerConnection.IceServer.builder("stun:stun2.l.google.com:19302").createIceServer(),
            // Provide multiple TURN transports so mobile networks can still obtain relay
            // candidates during renegotiation instead of relying only on host/srflx.
            PeerConnection.IceServer.builder(
                listOf(
                    "turn:openrelay.metered.ca:80",
                    "turn:openrelay.metered.ca:80?transport=tcp",
                    "turn:openrelay.metered.ca:443",
                    "turn:openrelay.metered.ca:443?transport=tcp",
                    "turns:openrelay.metered.ca:443?transport=tcp"
                )
            )
                .setUsername("openrelayproject")
                .setPassword("openrelayproject")
                .createIceServer()
        )
        val rtcConfig = PeerConnection.RTCConfiguration(iceServers).apply {
            sdpSemantics = PeerConnection.SdpSemantics.UNIFIED_PLAN
            continualGatheringPolicy = PeerConnection.ContinualGatheringPolicy.GATHER_CONTINUALLY
        }
        peerConnectionObserver = object : PeerConnection.Observer{
            override fun onSignalingChange(p0: PeerConnection.SignalingState?) {
                Log.d("WebRTC", "onSignalingChange: $p0")
            }

            override fun onIceConnectionChange(p0: PeerConnection.IceConnectionState?) {
                Log.d("WebRTC", "ICE connection state changed: $p0")
                if (
                    p0 == PeerConnection.IceConnectionState.CONNECTED ||
                    p0 == PeerConnection.IceConnectionState.COMPLETED
                ) {
                    checkForRemoteVideoTrack("ice-$p0")
                }
            }

            override fun onIceConnectionReceivingChange(p0: Boolean) {
                logMessage("initialize", { "onIceConnectionReceivingChange" })
            }

            override fun onIceGatheringChange(p0: PeerConnection.IceGatheringState?) {
                logMessage("initialize", { "onIceGatheringChange" })
            }

            override fun onIceCandidate(candidate: IceCandidate) {
                logMessage("initialize", { "onIceCandidate" })
                Log.d(
                    "WebRTC",
                    "onIceCandidate: mid=${candidate.sdpMid}, mLine=${candidate.sdpMLineIndex}, sdp=${candidate.sdp}"
                )
                val iceCandidateData = IceCandidateDTO(candidate.sdp, candidate.sdpMid, candidate.sdpMLineIndex)
                onIceCandidateCreated(iceCandidateData)
            }

            override fun onIceCandidatesRemoved(p0: Array<out IceCandidate?>?) {
                logMessage("initialize", { "onIceCandidatesRemoved" })
            }

            override fun onAddStream(stream: MediaStream?) {
                Log.d("WebRTC", "onAddStream: videoTracks=${stream?.videoTracks?.size}, audioTracks=${stream?.audioTracks?.size}")
                stream?.videoTracks?.firstOrNull()?.let { track ->
                    if (track !== remoteVideoTrack) {
                        Log.d("WebRTC", "Remote video track obtained via onAddStream fallback")
                        emitRemoteVideoTrack(track)
                    }
                }
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
                val receiver = transceiver?.receiver
                val mediaStreamTrack = runCatching { receiver?.track() }.getOrNull()
                Log.d("WebRTC", "onTrack: kind=${mediaStreamTrack?.kind()}, id=${runCatching { mediaStreamTrack?.id() }.getOrNull()}, enabled=${runCatching { mediaStreamTrack?.enabled() }.getOrNull()}")
                when (mediaStreamTrack) {
                    is VideoTrack -> {
                        Log.d("WebRTC", "onTrack: remote VIDEO track received")
                        val stableVideoTrack = runCatching { receiver?.track() as? VideoTrack }.getOrNull()
                        emitRemoteVideoTrack(stableVideoTrack ?: mediaStreamTrack)
                    }
                    is AudioTrack -> {
                        remoteAudioTrack = mediaStreamTrack
                        applySpeakerType(lastSpeakerType)
                        Log.d("WebRTC", "Remote audio track enabled: ${remoteAudioTrack?.enabled()}")
                    }
                }
            }
        }
        //Setup peer connection.
        peerConnection = peerConnectionFactory.createPeerConnection(rtcConfig,peerConnectionObserver)
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
        audioManager.isSpeakerphoneOn = false
    }

    /**
     * This function is used to start video call.
     * @Param:
     * onStartVideoCall: return local video track when it is available.
     * */
    override suspend fun startVideoCall(
        isVideoInitiator: Boolean,
        onStartVideoCall: suspend (videoTrack: WebRTCVideoTrack) -> Unit
    ) {
        // Only tear down and recreate when we're the video initiator (caller). The callee has
        // just set the remote description; calling stopVideoCallResources() here would set our
        // video transceiver to RECV_ONLY and break the answer path, so the other user would
        // see a black remote (callee's local track would not be sent).
        if (isVideoInitiator && hasStarted) {
            Log.d("WebRTC", "startVideoCall: initiator re-entry, cleaning up before full creation")
            stopVideoCallResources()
        }

        hasStarted = true

        // Stop any existing capturer from a previous attempt before re-creating
        videoCapturer?.let { capturer ->
            try { capturer.stopCapture() } catch (_: Exception) {}
            try { capturer.dispose() } catch (_: Exception) {}
        }
        videoCapturer = null

        videoCapturer = createCameraCapturer()
        val capturer = videoCapturer
        if (capturer == null) {
            Log.e("WebRTC", "No camera capturer available on this device.")
            hasStarted = false
            return
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

        // Dispose previous video source to avoid leaks from prior attempts
        localVideoSource?.dispose()
        localVideoSource = peerConnectionFactory.createVideoSource(capturer.isScreencast)

        try {
            capturer.initialize(surfaceTextureHelper, appContext, localVideoSource?.capturerObserver)
            startCaptureWithFallbacks(capturer)
        } catch (e: Exception) {
            Log.e("WebRTC", "Capturer start failed: ${e.message}")
            surfaceTextureHelper?.dispose()
            surfaceTextureHelper = null
            hasStarted = false
            return
        }

        localVideoTrack = peerConnectionFactory.createVideoTrack("video", localVideoSource)
        localVideoTrack?.setEnabled(true)

        val localTrack = localVideoTrack ?: run {
            Log.e("WebRTC", "startVideoCall: localVideoTrack is null")
            hasStarted = false
            return
        }

        peerConnection?.senders
            ?.filter { it.track() is VideoTrack }
            ?.forEach { sender ->
                try {
                    peerConnection?.removeTrack(sender)
                } catch (_: Exception) {}
            }
        localVideoSender = null // reset
        localVideoSender = peerConnection?.addTrack(localTrack)

        if (!isVideoSenderUsable(localVideoSender)) {
            Log.e("WebRTC", "startVideoCall: local video sender is unusable after attach")
        }

        localVideoTrack?.let { track ->
            emitLocalVideoTrack(track, onStartVideoCall)
        }
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

    private fun startCaptureWithFallbacks(capturer: CameraVideoCapturer) {
        val capturePresets = listOf(
            Triple(1280, 720, 30),
            Triple(960, 540, 24),
            Triple(640, 480, 24),
            Triple(320, 240, 15)
        )
        var lastError: Exception? = null
        for ((width, height, fps) in capturePresets) {
            try {
                capturer.startCapture(width, height, fps)
                Log.d("WebRTC", "Camera capture started at ${width}x${height}@${fps}")
                return
            } catch (e: Exception) {
                lastError = e
                Log.w("WebRTC", "Capture preset ${width}x${height}@${fps} failed: ${e.message}")
            }
        }
        throw IllegalStateException("Failed to start camera capture with all presets", lastError)
    }

    private fun normalizeVideoSdp(description: SessionDescription): SessionDescription {
        var sdp = description.description ?: return description
        // Keep SDP normalization intentionally conservative.
        // Aggressive codec/direction rewrites can make setLocalDescription fail on some devices.
        // We only normalize line endings for signaling transport/storage stability.
        sdp = sdp.replace("\r\n", "\n").replace("\n", "\r\n")

        if (!sdp.endsWith("\r\n")) {
            sdp += "\r\n"
        }

        return SessionDescription(description.type, sdp)
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

    override suspend fun prepareForIncomingVideoNegotiation() {
        val senderTrack = runCatching { localVideoSender?.track() as? VideoTrack }.getOrNull()
        if (hasStarted && isVideoTrackUsable(senderTrack)) {
            // Sync service's local track ref with the sender's live track.
            // Do NOT emit to CallEventFlow here — startVideoCall duplicate path handles emission
            // after the track is enabled and ready. Premature emission can cause DisposableEffect
            // to bind to a track that's still disabled or not yet fully attached.
            localVideoTrack = senderTrack
            Log.d(
                "WebRTC",
                "prepareForIncomingVideoNegotiation: active sender track exists, keep existing video resources"
            )
            return
        }
        val reusableSendPath = findReusableLocalVideoSendPath()
        if (hasStarted && (isVideoSenderUsable(localVideoSender) || reusableSendPath != null)) {
            if (reusableSendPath != null) {
                localVideoSender = reusableSendPath.first
                localVideoTrack = reusableSendPath.second
            }
            Log.d(
                "WebRTC",
                "prepareForIncomingVideoNegotiation: active video sender exists, skip resetting tracks/ICE gate"
            )
            return
        }
        Log.d(
            "WebRTC",
            "prepareForIncomingVideoNegotiation: queue remote ICE until new video SDP is applied"
        )
        // Clear potentially disposed/stale remote track while waiting for renegotiated video.
        remoteVideoTrack = null
        CallEventFlow.remoteVideoTrack.value = null
        isRemoteDescriptionSet = false
        // Do not clear pendingRemoteIceCandidates here. 
        // Video ICE candidates might have already arrived and been queued.
    }

    override suspend fun resetVideoCallStartedState() {
        // Keep video session state alive across Video -> Audio screen navigation.
        // Full reset must happen only in stopVideoCallResources()/releaseResources().
        Log.d("WebRTC", "resetVideoCallStartedState: ignore to preserve active video resources")
    }

    override suspend fun stopVideoCallResources() {
        Log.d("WebRTC", "stopVideoCallResources: cleaning up video-only resources")
        // Stop and dispose capturer
        videoCapturer?.let { capturer ->
            try { capturer.stopCapture() } catch (_: Exception) {}
            try { capturer.dispose() } catch (_: Exception) {}
        }
        videoCapturer = null

        // Detach video from sender while keeping transceiver reusable for next upgrade.
        localVideoSender?.let { sender ->
            val detached = runCatching { sender.setTrack(null, false) }
                .onFailure { e ->
                    Log.w("WebRTC", "stopVideoCallResources: setTrack(null) failed: ${e.message}")
                }
                .getOrDefault(false)
            val transceiverUpdated = runCatching {
                peerConnection?.transceivers
                    ?.firstOrNull { it.sender == sender }
                    ?.also { it.direction = RtpTransceiver.RtpTransceiverDirection.RECV_ONLY } != null
            }
                .onFailure { e ->
                    Log.w("WebRTC", "stopVideoCallResources: failed to mark video transceiver RECV_ONLY: ${e.message}")
                }
                .getOrDefault(false)
            Log.d(
                "WebRTC",
                "stopVideoCallResources: detached=$detached, transceiverUpdated=$transceiverUpdated"
            )
        }
        // Keep localVideoSender reference for reuse on next startVideoCall.

        // Dispose video source and track
        runCatching { localVideoSource?.dispose() }
        localVideoSource = null
        localVideoTrack = null
        remoteVideoTrack = null
        CallEventFlow.localVideoTrack.value = null
        CallEventFlow.remoteVideoTrack.value = null

        // Dispose surface texture helper
        runCatching { surfaceTextureHelper?.stopListening() }
        runCatching { surfaceTextureHelper?.dispose() }
        surfaceTextureHelper = null

        isRemoteDescriptionSet = true
        val pendingCount = pendingRemoteIceCandidates.size
        if (pendingCount > 0) {
            Log.d("WebRTC", "Flushing $pendingCount pending remote ICE candidates after video reject")
            pendingRemoteIceCandidates.forEach { candidate ->
                peerConnection?.addIceCandidate(candidate)
            }
            pendingRemoteIceCandidates.clear()
        }

        hasStarted = false
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
            remoteAudioTrack = null
            localVideoSender = null
            CallEventFlow.localVideoTrack.value = null
            CallEventFlow.remoteVideoTrack.value = null
            hasStarted = false
            onRemoteVideoTrackReceivedCallback = null

            // PeerConnection
            peerConnectionObserver = null
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
                audioDeviceModule.setSpeakerMute(false)
            }
            runCatching { audioDeviceModule.release() }

            logMessage("CallForegroundService") { "WebRTC resources released successfully" }

            // Clear singleton so the next call gets a fresh instance (new PeerConnectionFactory, EglBase, etc.)
            clearInstance()
        } catch (e: Exception) {
            logMessage("CallForegroundService") { "Failed to stop WebRTC cleanly: ${e.message}" }
            clearInstance()
        }
    }

    override suspend fun updateMuteStatus(muted: Boolean) {
        val audioManager =
            appContext.getSystemService(Context.AUDIO_SERVICE) as AudioManager

        audioManager.isMicrophoneMute = muted
        localAudioTrack?.setEnabled(!muted)
    }

    override suspend fun updateCameraStatus(cameraOff: Boolean) {
        val shouldEnable = !cameraOff
        runCatching { localVideoTrack?.setEnabled(shouldEnable) }
        runCatching { (localVideoSender?.track() as? VideoTrack)?.setEnabled(shouldEnable) }
    }

    override suspend fun updateSpeakerStatus(speakerType: SpeakerType) {
        lastSpeakerType = speakerType
        applySpeakerType(speakerType)
    }

    private fun getRemoteAudioTrack(): AudioTrack? {
        peerConnection?.transceivers?.forEach { transceiver ->
            val track = transceiver.receiver.track()
            if (track is AudioTrack) return track
        }
        return remoteAudioTrack
    }

    private fun applySpeakerType(speakerType: SpeakerType) {
        val audioManager =
            appContext.getSystemService(Context.AUDIO_SERVICE) as AudioManager
        audioManager.mode = AudioManager.MODE_IN_COMMUNICATION

        when (speakerType) {
            SpeakerType.Audio -> {
                audioDeviceModule.setSpeakerMute(false)
                getRemoteAudioTrack()?.setEnabled(true)
                remoteAudioTrack?.setEnabled(true)
                audioManager.isSpeakerphoneOn = false
            }
            SpeakerType.Speaker -> {
                audioDeviceModule.setSpeakerMute(false)
                getRemoteAudioTrack()?.setEnabled(true)
                remoteAudioTrack?.setEnabled(true)
                audioManager.isSpeakerphoneOn = true
            }
        }
    }
}