package com.minhtu.firesocialmedia.domain.usecases.call

import com.minhtu.firesocialmedia.domain.entity.call.CallStatus
import com.minhtu.firesocialmedia.domain.entity.call.SpeakerType
import com.minhtu.firesocialmedia.testutil.FakeCallRepository
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class ManageCallStateUseCaseTest {

    @Test
    fun `acceptCall sends ACCEPTED status`() = runTest {
        val repo = FakeCallRepository(sendCallStatusToFirebaseResult = true)
        val useCase = ManageCallStateUseCase(repo)

        val result = useCase.acceptCall("session1")

        assertTrue(result)
        assertEquals("session1" to CallStatus.ACCEPTED, repo.sendCallStatusToFirebaseCalledWith)
    }

    @Test
    fun `rejectCall returns false for empty sessionId without calling repository`() = runTest {
        val repo = FakeCallRepository()
        val useCase = ManageCallStateUseCase(repo)

        val result = useCase.rejectCall("")

        assertFalse(result)
        assertEquals(null, repo.sendCallStatusToFirebaseCalledWith)
    }

    @Test
    fun `rejectCall sends ENDED status for non-empty sessionId`() = runTest {
        val repo = FakeCallRepository(sendCallStatusToFirebaseResult = true)
        val useCase = ManageCallStateUseCase(repo)

        val result = useCase.rejectCall("session1")

        assertTrue(result)
        assertEquals("session1" to CallStatus.ENDED, repo.sendCallStatusToFirebaseCalledWith)
    }

    @Test
    fun `sendWhoEndCall returns false for empty sessionId`() = runTest {
        val repo = FakeCallRepository()
        val useCase = ManageCallStateUseCase(repo)

        val result = useCase.sendWhoEndCall("", "user1")

        assertFalse(result)
        assertEquals(null, repo.sendWhoEndCallCalledWith)
    }

    @Test
    fun `sendWhoEndCall delegates to repository for non-empty sessionId`() = runTest {
        val repo = FakeCallRepository(sendWhoEndCallResult = true)
        val useCase = ManageCallStateUseCase(repo)

        val result = useCase.sendWhoEndCall("session1", "user1")

        assertTrue(result)
        assertEquals("session1" to "user1", repo.sendWhoEndCallCalledWith)
    }

    @Test
    fun `endCall returns false for empty sessionId without calling repository`() = runTest {
        val repo = FakeCallRepository()
        val useCase = ManageCallStateUseCase(repo)

        val result = useCase.endCall("")

        assertFalse(result)
        assertEquals(null, repo.deleteCallSessionCalledWith)
    }

    @Test
    fun `endCall deletes session for non-empty sessionId`() = runTest {
        val repo = FakeCallRepository(deleteCallSessionResult = true)
        val useCase = ManageCallStateUseCase(repo)

        val result = useCase.endCall("session1")

        assertTrue(result)
        assertEquals("session1", repo.deleteCallSessionCalledWith)
    }

    @Test
    fun `acceptCallFromApp delegates to repository`() = runTest {
        val repo = FakeCallRepository()
        val useCase = ManageCallStateUseCase(repo)

        useCase.acceptCallFromApp("session1", "callee1")

        assertEquals("session1" to "callee1", repo.acceptCallFromAppCalledWith)
    }

    @Test
    fun `callerEndCallFromApp delegates to repository`() = runTest {
        val repo = FakeCallRepository()
        val useCase = ManageCallStateUseCase(repo)

        useCase.callerEndCallFromApp("caller1")

        assertEquals("caller1", repo.callerEndCallInvokedWith)
    }

    @Test
    fun `calleeEndCallFromApp delegates to repository`() = runTest {
        val repo = FakeCallRepository()
        val useCase = ManageCallStateUseCase(repo)

        useCase.calleeEndCallFromApp("session1", "callee1")

        assertEquals("session1" to "callee1", repo.calleeEndCallInvokedWith)
    }

    @Test
    fun `rejectVideoCall delegates to repository`() = runTest {
        val repo = FakeCallRepository()
        val useCase = ManageCallStateUseCase(repo)

        useCase.rejectVideoCall()

        assertTrue(repo.rejectVideoCallInvoked)
    }

    @Test
    fun `resetVideoCallStartedState delegates to repository`() = runTest {
        val repo = FakeCallRepository()
        val useCase = ManageCallStateUseCase(repo)

        useCase.resetVideoCallStartedState()

        assertTrue(repo.resetVideoCallStartedStateInvoked)
    }

    @Test
    fun `stopVideoCallResources delegates to repository`() = runTest {
        val repo = FakeCallRepository()
        val useCase = ManageCallStateUseCase(repo)

        useCase.stopVideoCallResources()

        assertTrue(repo.stopVideoCallResourcesInvoked)
    }

    @Test
    fun `updateMuteStatus delegates to repository`() = runTest {
        val repo = FakeCallRepository()
        val useCase = ManageCallStateUseCase(repo)

        useCase.updateMuteStatus(true)

        assertEquals(true, repo.muteStatus)
    }

    @Test
    fun `updateSpeakerStatus delegates to repository`() = runTest {
        val repo = FakeCallRepository()
        val useCase = ManageCallStateUseCase(repo)

        useCase.updateSpeakerStatus(SpeakerType.Speaker)

        assertEquals(SpeakerType.Speaker, repo.speakerStatus)
    }
}
