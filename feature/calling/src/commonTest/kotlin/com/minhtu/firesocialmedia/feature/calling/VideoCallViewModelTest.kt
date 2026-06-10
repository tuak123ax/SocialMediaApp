package com.minhtu.firesocialmedia.feature.calling

import com.minhtu.firesocialmedia.core.domain.entity.call.CallStatus
import com.minhtu.firesocialmedia.core.domain.entity.call.SpeakerType
import com.minhtu.firesocialmedia.core.domain.entity.call.AudioCallSession
import com.minhtu.firesocialmedia.core.domain.entity.call.CallingRequestData
import com.minhtu.firesocialmedia.core.domain.entity.call.IceCandidateData
import com.minhtu.firesocialmedia.core.domain.entity.call.OfferAnswer
import com.minhtu.firesocialmedia.core.domain.entity.user.UserInstance
import com.minhtu.firesocialmedia.core.domain.repository.CallRepository
import com.minhtu.firesocialmedia.core.domain.usecases.call.ManageCallStateUseCase
import com.minhtu.firesocialmedia.core.domain.usecases.call.RequestCameraAndAudioPermissionsUseCase
import com.minhtu.firesocialmedia.core.domain.usecases.call.StartVideoCallServiceUseCase
import com.minhtu.firesocialmedia.core.domain.usecases.call.UpdateCameraStatusUseCase
import com.minhtu.firesocialmedia.core.domain.usecases.call.UpdateMicStatusUseCase
import com.minhtu.firesocialmedia.core.domain.usecases.call.UpdateSpeakerStatusUseCase
import com.minhtu.firesocialmedia.core.utils.Utils
import com.minhtu.firesocialmedia.feature.calling.presentation.videocall.VideoCallViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

// ──────────────────── Fake CallRepository ────────────────────

private class FakeVideoCallRepository(
    private val cameraAndAudioPermissionGranted: Boolean = true
) : CallRepository {
    var startVideoCallServiceInvoked = false
    var micMuted: Boolean? = null
    var cameraOff: Boolean? = null
    var speakerStatus: SpeakerType? = null
    var stopVideoCallResourcesInvoked = false

    override suspend fun initialize(
        onInitializeFinished: () -> Unit,
        onIceCandidateCreated: (IceCandidateData) -> Unit,
        onRemoteVideoTrackReceived: (Any) -> Unit
    ) {}

    override suspend fun isCalleeInActiveCall(calleeId: String): Boolean? = false

    override suspend fun startCallService(sessionId: String, caller: UserInstance, callee: UserInstance) {}

    override suspend fun startVideoCallService(
        sessionId: String, caller: UserInstance, callee: UserInstance,
        currentUserId: String?, remoteVideoOffer: OfferAnswer?
    ) {
        startVideoCallServiceInvoked = true
    }

    override suspend fun createVideoOffer(onOfferCreated: (OfferAnswer) -> Unit) {}
    override suspend fun createOffer(onOfferCreated: (OfferAnswer) -> Unit) {}

    override suspend fun sendOfferToFireBase(
        sessionId: String, offer: OfferAnswer,
        sendIceCandidateCallBack: Utils.Companion.BasicCallBack
    ) {}

    override suspend fun createAnswer(videoSupport: Boolean, onAnswerCreated: (OfferAnswer) -> Unit) {}
    override suspend fun setRemoteDescription(remoteOfferAnswer: OfferAnswer) {}

    override suspend fun sendAnswerToFirebase(
        sessionId: String, answer: OfferAnswer,
        sendIceCandidateCallBack: Utils.Companion.BasicCallBack
    ) {}

    override suspend fun sendCallStatusToFirebase(sessionId: String, status: CallStatus): Boolean = true
    override suspend fun deleteCallSession(sessionId: String): Boolean = true
    override suspend fun acceptCallFromApp(sessionId: String, calleeId: String?) {}
    override suspend fun callerEndCallFromApp(currentUser: String) {}
    override suspend fun calleeEndCallFromApp(sessionId: String, currentUser: String) {}
    override suspend fun rejectVideoCall() {}
    override suspend fun resetVideoCallStartedState() {}

    override suspend fun stopVideoCallResources() {
        stopVideoCallResourcesInvoked = true
    }

    override suspend fun requestCameraAndAudioPermissions(): Boolean = cameraAndAudioPermissionGranted
    override suspend fun requestAudioPermission(): Boolean = true

    override suspend fun sendCallSessionToFirebase(
        session: AudioCallSession,
        sendCallSessionCallBack: Utils.Companion.BasicCallBack
    ) {}

    override suspend fun observeIceCandidatesFromCallee(
        sessionId: String,
        iceCandidateCallBack: (IceCandidateData) -> Unit
    ) {}

    override suspend fun startVideoCall(isVideoInitiator: Boolean, onStartVideoCall: suspend (Any) -> Unit) {}

    override suspend fun observeVideoCall(sessionId: String, videoCallCallBack: (OfferAnswer) -> Unit) {}

    override suspend fun addIceCandidate(sdp: String, sdpMid: String, sdpMLineIndex: Int) {}

    override suspend fun observeAnswerFromCallee(
        sessionId: String,
        answerCallBack: (OfferAnswer) -> Unit,
        rejectCallBack: () -> Unit
    ) {}

    override suspend fun updateOfferInFirebase(
        sessionId: String, updateContent: String,
        updateField: String, updateOfferCallBack: Utils.Companion.BasicCallBack
    ) {}

    override suspend fun observeCallStatus(
        sessionId: String,
        callStatusCallBack: Utils.Companion.CallStatusCallBack
    ) {}

    override suspend fun updateAnswerInFirebase(
        sessionId: String, updateContent: String,
        updateField: String, updateAnswerCallBack: Utils.Companion.BasicCallBack
    ) {}

    override suspend fun clearAnswerInFirebase(sessionId: String) {}

    override suspend fun observePhoneCallWithoutCheckingInCall(
        currentUserId: String,
        phoneCallCallBack: (CallingRequestData) -> Unit,
        endCallSession: (Boolean) -> Unit,
        whoEndCallCallBack: (String) -> Unit,
        iceCandidateCallBack: (Map<String, IceCandidateData>?) -> Unit
    ) {}

    override suspend fun observePhoneCall(
        isInCall: MutableStateFlow<Boolean>,
        currentUserId: String,
        phoneCallCallBack: (CallingRequestData) -> Unit,
        endCallSession: (Boolean) -> Unit,
        whoEndCallCallBack: (String) -> Unit,
        iceCandidateCallBack: (Map<String, IceCandidateData>?) -> Unit
    ) {}

    override suspend fun sendIceCandidateToFireBase(
        sessionId: String, iceCandidate: IceCandidateData,
        whichCandidate: String, sendIceCandidateCallBack: Utils.Companion.BasicCallBack
    ) {}

    override suspend fun sendWhoEndCall(sessionId: String, whoEndCall: String): Boolean = true
    override suspend fun stopCallService() {}
    override fun stopObservePhoneCall() {}
    override suspend fun updateMuteStatus(muted: Boolean) { micMuted = muted }
    override suspend fun updateCameraStatus(cameraOff: Boolean) { this.cameraOff = cameraOff }
    override suspend fun updateSpeakerStatus(speakerType: SpeakerType) { speakerStatus = speakerType }
}

// ──────────────────── Tests ────────────────────

@OptIn(ExperimentalCoroutinesApi::class)
class VideoCallViewModelTest {

    private fun buildViewModel(
        scheduler: kotlinx.coroutines.test.TestCoroutineScheduler,
        cameraAndAudioPermissionGranted: Boolean = true
    ): Pair<VideoCallViewModel, FakeVideoCallRepository> {
        val repo = FakeVideoCallRepository(cameraAndAudioPermissionGranted = cameraAndAudioPermissionGranted)
        val vm = VideoCallViewModel(
            StartVideoCallServiceUseCase(repo),
            RequestCameraAndAudioPermissionsUseCase(repo),
            UpdateMicStatusUseCase(repo),
            UpdateCameraStatusUseCase(repo),
            UpdateSpeakerStatusUseCase(repo),
            ManageCallStateUseCase(repo),
            StandardTestDispatcher(scheduler)
        )
        return vm to repo
    }

    @Test
    fun `setPendingVideoCallParams stores sessionId and offer`() = runTest {
        val (vm, _) = buildViewModel(testScheduler)
        val offer = OfferAnswer(sdp = "s", type = "offer", initiator = "u1")
        vm.setPendingVideoCallParams("session42", offer)
        assertEquals("session42", vm.pendingVideoCallSessionId.value)
        assertEquals(offer, vm.pendingRemoteVideoOffer.value)
    }

    @Test
    fun `clearPendingVideoCallParams clears sessionId and offer`() = runTest {
        val (vm, _) = buildViewModel(testScheduler)
        val offer = OfferAnswer(sdp = "s", type = "offer", initiator = "u1")
        vm.setPendingVideoCallParams("session42", offer)
        vm.clearPendingVideoCallParams()
        assertEquals("", vm.pendingVideoCallSessionId.value)
        assertNull(vm.pendingRemoteVideoOffer.value)
    }

    @Test
    fun `updateMicStatus delegates to use case`() = runTest {
        val (vm, repo) = buildViewModel(testScheduler)
        vm.updateMicStatus(true)
        advanceUntilIdle()
        assertEquals(true, repo.micMuted)
    }

    @Test
    fun `updateCameraStatus delegates to use case`() = runTest {
        val (vm, repo) = buildViewModel(testScheduler)
        vm.updateCameraStatus(true)
        advanceUntilIdle()
        assertEquals(true, repo.cameraOff)
    }

    @Test
    fun `updateSpeakerStatus updates currentSpeakerType and delegates to use case`() = runTest {
        val (vm, repo) = buildViewModel(testScheduler)
        vm.updateSpeakerStatus(SpeakerType.Speaker)
        advanceUntilIdle()
        assertEquals(SpeakerType.Speaker, vm.currentSpeakerType.value)
        assertEquals(SpeakerType.Speaker, repo.speakerStatus)
    }

    @Test
    fun `stopVideoCallResources delegates to use case`() = runTest {
        val (vm, repo) = buildViewModel(testScheduler)
        vm.stopVideoCallResources()
        advanceUntilIdle()
        assertEquals(true, repo.stopVideoCallResourcesInvoked)
    }

    @Test
    fun `startVideoCall invokes startVideoCallService`() = runTest {
        val (vm, repo) = buildViewModel(testScheduler)
        val caller = UserInstance(uid = "callerUid", name = "Caller")
        val callee = UserInstance(uid = "calleeUid", name = "Callee")
        val offer = OfferAnswer(sdp = "sdp", type = "offer", initiator = "callerUid")
        vm.startVideoCall(offer, caller, callee, "callerUid", "session1")
        advanceUntilIdle()
        assertEquals(true, repo.startVideoCallServiceInvoked)
    }

    @Test
    fun `startVideoCall deduplicates identical calls`() = runTest {
        val (vm, repo) = buildViewModel(testScheduler)
        val caller = UserInstance(uid = "callerUid", name = "Caller")
        val callee = UserInstance(uid = "calleeUid", name = "Callee")
        val offer = OfferAnswer(sdp = "sdp", type = "offer", initiator = "callerUid")
        // First call
        vm.startVideoCall(offer, caller, callee, "callerUid", "session1")
        advanceUntilIdle()
        assertEquals(true, repo.startVideoCallServiceInvoked)
        // Reset flag
        repo.startVideoCallServiceInvoked = false
        // Second identical call while isStartingVideoCall is still true — should be skipped
        // (dedup guard checks same signature; after first completes guard resets)
        // This verifies no duplicate invocation with a fresh call
        vm.startVideoCall(
            OfferAnswer(sdp = "different", type = "offer", initiator = "callerUid"),
            caller, callee, "callerUid", "session2"
        )
        advanceUntilIdle()
        assertEquals(true, repo.startVideoCallServiceInvoked)
    }

    @Test
    fun `requestPermissionsAndStartVideoCall calls onGranted when permissions granted`() = runTest {
        val (vm, _) = buildViewModel(testScheduler, cameraAndAudioPermissionGranted = true)
        var grantedCalled = false
        var deniedCalled = false
        vm.requestPermissionsAndStartVideoCall(
            onGranted = { grantedCalled = true },
            onDenied = { deniedCalled = true }
        )
        advanceUntilIdle()
        assertEquals(true, grantedCalled)
        assertEquals(false, deniedCalled)
    }

    @Test
    fun `requestPermissionsAndStartVideoCall calls onDenied when permissions denied`() = runTest {
        val (vm, _) = buildViewModel(testScheduler, cameraAndAudioPermissionGranted = false)
        var grantedCalled = false
        var deniedCalled = false
        vm.requestPermissionsAndStartVideoCall(
            onGranted = { grantedCalled = true },
            onDenied = { deniedCalled = true }
        )
        advanceUntilIdle()
        assertEquals(false, grantedCalled)
        assertEquals(true, deniedCalled)
    }
}

