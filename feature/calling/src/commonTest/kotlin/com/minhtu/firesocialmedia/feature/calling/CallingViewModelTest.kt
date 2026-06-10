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
import com.minhtu.firesocialmedia.core.domain.usecases.call.RequestPermissionUseCase
import com.minhtu.firesocialmedia.core.domain.usecases.call.StartCallServiceUseCase
import com.minhtu.firesocialmedia.core.utils.Utils
import com.minhtu.firesocialmedia.feature.calling.presentation.audiocall.CallingViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

// ──────────────────── Fake CallRepository ────────────────────

private class FakeCallRepository(
    private val audioPermissionGranted: Boolean = true,
    private val cameraPermissionGranted: Boolean = true
) : CallRepository {
    var startCallServiceInvoked = false
    var callerEndCallInvokedWith: String? = null
    var calleeEndCallInvokedWith: Pair<String, String>? = null
    var muteStatus: Boolean? = null
    var speakerStatus: SpeakerType? = null

    override suspend fun initialize(
        onInitializeFinished: () -> Unit,
        onIceCandidateCreated: (IceCandidateData) -> Unit,
        onRemoteVideoTrackReceived: (Any) -> Unit
    ) {}

    override suspend fun isCalleeInActiveCall(calleeId: String): Boolean? = false

    override suspend fun startCallService(sessionId: String, caller: UserInstance, callee: UserInstance) {
        startCallServiceInvoked = true
    }

    override suspend fun startVideoCallService(
        sessionId: String, caller: UserInstance, callee: UserInstance,
        currentUserId: String?, remoteVideoOffer: OfferAnswer?
    ) {}

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

    override suspend fun callerEndCallFromApp(currentUser: String) {
        callerEndCallInvokedWith = currentUser
    }

    override suspend fun calleeEndCallFromApp(sessionId: String, currentUser: String) {
        calleeEndCallInvokedWith = sessionId to currentUser
    }

    override suspend fun rejectVideoCall() {}
    override suspend fun resetVideoCallStartedState() {}
    override suspend fun stopVideoCallResources() {}

    override suspend fun requestCameraAndAudioPermissions(): Boolean = cameraPermissionGranted
    override suspend fun requestAudioPermission(): Boolean = audioPermissionGranted

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
    override suspend fun updateMuteStatus(muted: Boolean) { muteStatus = muted }
    override suspend fun updateCameraStatus(cameraOff: Boolean) {}
    override suspend fun updateSpeakerStatus(speakerType: SpeakerType) { speakerStatus = speakerType }
}

// ──────────────────── Tests ────────────────────

@OptIn(ExperimentalCoroutinesApi::class)
class CallingViewModelTest {

    private fun buildViewModel(
        scheduler: kotlinx.coroutines.test.TestCoroutineScheduler,
        audioPermissionGranted: Boolean = true
    ): Pair<CallingViewModel, FakeCallRepository> {
        val repo = FakeCallRepository(audioPermissionGranted = audioPermissionGranted)
        val vm = CallingViewModel(
            StartCallServiceUseCase(repo),
            ManageCallStateUseCase(repo),
            RequestPermissionUseCase(repo),
            StandardTestDispatcher(scheduler)
        )
        return vm to repo
    }

    @Test
    fun `generateSessionId returns sorted joined string`() = runTest {
        val (vm, _) = buildViewModel(testScheduler)
        val id = vm.generateSessionId("userB", "userA")
        assertEquals("userA_userB", id)
    }

    @Test
    fun `generateSessionId with same order still sorts`() = runTest {
        val (vm, _) = buildViewModel(testScheduler)
        val id = vm.generateSessionId("alice", "bob")
        assertEquals("alice_bob", id)
    }

    @Test
    fun `updateSessionId updates session id field`() = runTest {
        val (vm, _) = buildViewModel(testScheduler)
        vm.updateSessionId("session123")
        assertEquals("session123", vm.sessionId)
    }

    @Test
    fun `count increments secondsForCountUpTimer`() = runTest {
        val (vm, _) = buildViewModel(testScheduler)
        vm.count()
        vm.count()
        assertEquals(2, vm.secondsForCountUpTimer.value)
    }

    @Test
    fun `resetCounter sets secondsForCountUpTimer to zero`() = runTest {
        val (vm, _) = buildViewModel(testScheduler)
        vm.count()
        vm.count()
        vm.count()
        vm.resetCounter()
        assertEquals(0, vm.secondsForCountUpTimer.value)
    }

    @Test
    fun `resetMuteAndSpeakerState resets isMuted and speakerType`() = runTest {
        val (vm, _) = buildViewModel(testScheduler)
        vm.isMuted.value = true
        vm.currentSpeakerType.value = SpeakerType.Speaker
        vm.resetMuteAndSpeakerState()
        assertEquals(false, vm.isMuted.value)
        assertEquals(SpeakerType.Audio, vm.currentSpeakerType.value)
    }

    @Test
    fun `getSessionId returns provided id when not empty`() = runTest {
        val (vm, _) = buildViewModel(testScheduler)
        vm.updateSessionId("fallback")
        assertEquals("provided", vm.getSessionId("provided"))
    }

    @Test
    fun `getSessionId falls back to stored sessionId when given empty string`() = runTest {
        val (vm, _) = buildViewModel(testScheduler)
        vm.updateSessionId("stored123")
        assertEquals("stored123", vm.getSessionId(""))
    }

    @Test
    fun `setPendingVideoOfferForAccept stores offer and sessionId`() = runTest {
        val (vm, _) = buildViewModel(testScheduler)
        val offer = OfferAnswer(sdp = "sdp1", type = "offer", initiator = "u1")
        vm.setPendingVideoOfferForAccept(offer, "session99")
        assertEquals(offer, vm.pendingVideoOfferForAccept.value)
        assertEquals("session99", vm.pendingSessionIdForVideoCall.value)
    }

    @Test
    fun `clearPendingVideoOfferForAccept clears offer and sessionId`() = runTest {
        val (vm, _) = buildViewModel(testScheduler)
        val offer = OfferAnswer(sdp = "sdp1", type = "offer", initiator = "u1")
        vm.setPendingVideoOfferForAccept(offer, "session99")
        vm.clearPendingVideoOfferForAccept()
        assertNull(vm.pendingVideoOfferForAccept.value)
        assertEquals("", vm.pendingSessionIdForVideoCall.value)
    }

    @Test
    fun `updateMuteStatus updates isMuted and delegates to use case`() = runTest {
        val (vm, repo) = buildViewModel(testScheduler)
        vm.updateMuteStatus(true)
        advanceUntilIdle()
        assertEquals(true, vm.isMuted.value)
        assertEquals(true, repo.muteStatus)
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
    fun `startCall invokes startCallService with generated sessionId`() = runTest {
        val (vm, repo) = buildViewModel(testScheduler)
        val caller = UserInstance(uid = "callerUid", name = "Caller")
        val callee = UserInstance(uid = "calleeUid", name = "Callee")
        vm.startCall(caller, callee)
        advanceUntilIdle()
        assertEquals(true, repo.startCallServiceInvoked)
        assertEquals("calleeUid_callerUid", vm.sessionId)
    }

    @Test
    fun `stopCall as caller invokes callerEndCallFromApp`() = runTest {
        val (vm, repo) = buildViewModel(testScheduler)
        vm.updateSessionId("s1")
        vm.stopCall(isCaller = true, currentUser = "callerUid")
        assertEquals("callerUid", repo.callerEndCallInvokedWith)
        assertEquals("", vm.sessionId)
    }

    @Test
    fun `stopCall as callee invokes calleeEndCallFromApp`() = runTest {
        val (vm, repo) = buildViewModel(testScheduler)
        vm.updateSessionId("sess123")
        vm.stopCall(isCaller = false, currentUser = "calleeUid")
        assertEquals("sess123" to "calleeUid", repo.calleeEndCallInvokedWith)
        assertEquals("", vm.sessionId)
    }

    @Test
    fun `requestPermissionAndStartAudioCall calls onGranted when permission granted`() = runTest {
        val (vm, _) = buildViewModel(testScheduler, audioPermissionGranted = true)
        var grantedCalled = false
        var deniedCalled = false
        vm.requestPermissionAndStartAudioCall(
            onGranted = { grantedCalled = true },
            onDenied = { deniedCalled = true }
        )
        advanceUntilIdle()
        assertEquals(true, grantedCalled)
        assertEquals(false, deniedCalled)
    }

    @Test
    fun `requestPermissionAndStartAudioCall calls onDenied when permission denied`() = runTest {
        val (vm, _) = buildViewModel(testScheduler, audioPermissionGranted = false)
        var grantedCalled = false
        var deniedCalled = false
        vm.requestPermissionAndStartAudioCall(
            onGranted = { grantedCalled = true },
            onDenied = { deniedCalled = true }
        )
        advanceUntilIdle()
        assertEquals(false, grantedCalled)
        assertEquals(true, deniedCalled)
    }
}


