package com.minhtu.firesocialmedia.presentation.audiocall

import com.minhtu.firesocialmedia.calling.entity.user.UserInstance
import com.minhtu.firesocialmedia.domain.entity.call.CallEventFlow
import com.minhtu.firesocialmedia.domain.entity.call.OfferAnswer
import com.minhtu.firesocialmedia.domain.entity.call.SpeakerType
import com.minhtu.firesocialmedia.domain.usecases.call.ManageCallStateUseCase
import com.minhtu.firesocialmedia.domain.usecases.call.RequestPermissionUseCase
import com.minhtu.firesocialmedia.domain.usecases.call.StartCallServiceUseCase
import com.minhtu.firesocialmedia.testutil.FakeCallRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestCoroutineScheduler
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class CallingViewModelTest {

    @BeforeTest
    @AfterTest
    fun resetGlobalState() {
        CallEventFlow.reset()
    }

    private fun buildViewModel(
        scheduler: TestCoroutineScheduler,
        audioPermissionGranted: Boolean = true
    ): Pair<CallingViewModel, FakeCallRepository> {
        val repo = FakeCallRepository(requestAudioPermissionResult = audioPermissionGranted)
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
    fun `generateSessionId with already sorted ids stays stable`() = runTest {
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
        assertEquals(Triple("calleeUid_callerUid", caller, callee), repo.startCallServiceCalledWith)
        assertEquals("calleeUid_callerUid", vm.sessionId)
    }

    @Test
    fun `stopCall as caller invokes callerEndCallFromApp and resets state`() = runTest {
        val (vm, repo) = buildViewModel(testScheduler)
        vm.updateSessionId("s1")
        vm.isMuted.value = true
        vm.stopCall(isCaller = true, currentUser = "callerUid")
        assertEquals("callerUid", repo.callerEndCallInvokedWith)
        assertEquals("", vm.sessionId)
        assertEquals(false, vm.isMuted.value)
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

    @Test
    fun `acceptCall delegates to manageCallStateUseCase with sessionId and calleeId`() = runTest {
        val (vm, repo) = buildViewModel(testScheduler)
        val callee = UserInstance(uid = "calleeUid", name = "Callee")
        vm.acceptCall("session1", callee)
        advanceUntilIdle()
        assertEquals("session1" to "calleeUid", repo.acceptCallFromAppCalledWith)
    }

    @Test
    fun `rejectVideoCall delegates to manageCallStateUseCase`() = runTest {
        val (vm, repo) = buildViewModel(testScheduler)
        vm.rejectVideoCall()
        advanceUntilIdle()
        assertTrue(repo.rejectVideoCallInvoked)
    }

    @Test
    fun `updateVideoState clears videoCallState`() = runTest {
        val (vm, _) = buildViewModel(testScheduler)
        CallEventFlow.videoCallState.value = OfferAnswer(sdp = "s", type = "offer", initiator = "u")
        vm.updateVideoState()
        assertNull(CallEventFlow.videoCallState.value)
    }

    @Test
    fun `syncAudioStateToService pushes current mute and speaker state`() = runTest {
        val (vm, repo) = buildViewModel(testScheduler)
        vm.isMuted.value = true
        vm.currentSpeakerType.value = SpeakerType.Speaker
        vm.syncAudioStateToService()
        advanceUntilIdle()
        assertEquals(true, repo.muteStatus)
        assertEquals(SpeakerType.Speaker, repo.speakerStatus)
    }
}
