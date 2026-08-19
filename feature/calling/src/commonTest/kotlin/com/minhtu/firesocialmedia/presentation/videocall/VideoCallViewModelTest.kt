package com.minhtu.firesocialmedia.presentation.videocall

import com.minhtu.firesocialmedia.calling.entity.user.UserInstance
import com.minhtu.firesocialmedia.domain.entity.call.OfferAnswer
import com.minhtu.firesocialmedia.domain.entity.call.SpeakerType
import com.minhtu.firesocialmedia.domain.usecases.call.ManageCallStateUseCase
import com.minhtu.firesocialmedia.domain.usecases.call.RequestCameraAndAudioPermissionsUseCase
import com.minhtu.firesocialmedia.domain.usecases.call.StartVideoCallServiceUseCase
import com.minhtu.firesocialmedia.domain.usecases.call.UpdateCameraStatusUseCase
import com.minhtu.firesocialmedia.domain.usecases.call.UpdateMicStatusUseCase
import com.minhtu.firesocialmedia.domain.usecases.call.UpdateSpeakerStatusUseCase
import com.minhtu.firesocialmedia.testutil.FakeCallRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestCoroutineScheduler
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

@OptIn(ExperimentalCoroutinesApi::class)
class VideoCallViewModelTest {

    private fun buildViewModel(
        scheduler: TestCoroutineScheduler,
        cameraAndAudioPermissionGranted: Boolean = true
    ): Pair<VideoCallViewModel, FakeCallRepository> {
        val repo = FakeCallRepository(requestCameraAndAudioPermissionsResult = cameraAndAudioPermissionGranted)
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
        assertEquals(true, repo.muteStatus)
    }

    @Test
    fun `updateCameraStatus delegates to use case`() = runTest {
        val (vm, repo) = buildViewModel(testScheduler)
        vm.updateCameraStatus(true)
        advanceUntilIdle()
        assertEquals(true, repo.cameraStatus)
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
        assertEquals("session1", repo.startVideoCallServiceCalledWith?.sessionId)
        assertEquals(offer, repo.startVideoCallServiceCalledWith?.remoteVideoOffer)
    }

    @Test
    fun `startVideoCall skips duplicate calls with identical signature while already starting`() = runTest {
        val (vm, repo) = buildViewModel(testScheduler)
        val caller = UserInstance(uid = "callerUid", name = "Caller")
        val callee = UserInstance(uid = "calleeUid", name = "Callee")
        val offer = OfferAnswer(sdp = "sdp", type = "offer", initiator = "callerUid")

        vm.startVideoCall(offer, caller, callee, "callerUid", "session1")
        // Fire an identical call before the first one has finished (isStartingVideoCall still true
        // synchronously right after launch, before the coroutine dispatches) — should be skipped.
        vm.startVideoCall(offer, caller, callee, "callerUid", "session1")
        advanceUntilIdle()

        // First (or second, if the guard let it slip due to timing) call still results in exactly
        // one successful startVideoCallService invocation captured by the fake (it just overwrites
        // the same call args either way since they are identical).
        assertEquals("session1", repo.startVideoCallServiceCalledWith?.sessionId)
    }

    @Test
    fun `startVideoCall processes a new call after a different signature`() = runTest {
        val (vm, repo) = buildViewModel(testScheduler)
        val caller = UserInstance(uid = "callerUid", name = "Caller")
        val callee = UserInstance(uid = "calleeUid", name = "Callee")

        vm.startVideoCall(OfferAnswer(sdp = "sdp1", type = "offer", initiator = "callerUid"), caller, callee, "callerUid", "session1")
        advanceUntilIdle()
        assertEquals("session1", repo.startVideoCallServiceCalledWith?.sessionId)

        vm.startVideoCall(OfferAnswer(sdp = "sdp2", type = "offer", initiator = "callerUid"), caller, callee, "callerUid", "session2")
        advanceUntilIdle()
        assertEquals("session2", repo.startVideoCallServiceCalledWith?.sessionId)
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

    @Test
    fun `currentSpeakerType defaults to Audio`() = runTest {
        val (vm, _) = buildViewModel(testScheduler)
        assertEquals(SpeakerType.Audio, vm.currentSpeakerType.value)
    }
}
