package com.minhtu.firesocialmedia.domain.usecases.call

import com.minhtu.firesocialmedia.domain.entity.call.AudioCallSession
import com.minhtu.firesocialmedia.domain.entity.call.IceCandidateData
import com.minhtu.firesocialmedia.domain.entity.call.OfferAnswer
import com.minhtu.firesocialmedia.testutil.FakeCallRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestCoroutineScheduler
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class CallerUseCasesTest {

    private fun scope(scheduler: TestCoroutineScheduler) = CoroutineScope(StandardTestDispatcher(scheduler))

    @Test
    fun `StartCallUseCase creates offer and sends call session on success`() = runTest {
        val repo = FakeCallRepository(
            createOfferResult = OfferAnswer(sdp = "offer-sdp", type = "offer", initiator = "caller"),
            sendCallSessionToFirebaseSuccess = true
        )
        val initializeCallUseCase = InitializeCallUseCase(repo, scope(testScheduler))
        val signalingUseCase = SendSignalingDataUseCase(repo, scope(testScheduler))
        val useCase = StartCallUseCase(initializeCallUseCase, signalingUseCase, scope(testScheduler))
        val session = AudioCallSession(sessionId = "session1")
        var sendResult: Boolean? = null
        var errorCaught: Exception? = null

        useCase.invoke(
            session,
            onIceCandidateCreated = {},
            onSendCallSession = { sendResult = it },
            onError = { errorCaught = it }
        )
        advanceUntilIdle()

        assertEquals(true, sendResult)
        assertNull(errorCaught)
        assertEquals("offer-sdp", session.offer?.sdp)
        assertEquals(session, repo.sendCallSessionToFirebaseCalledWith)
    }

    @Test
    fun `StartCallUseCase reports error when initialization throws`() = runTest {
        val repo = FakeCallRepository(initializeShouldThrow = true)
        val initializeCallUseCase = InitializeCallUseCase(repo, scope(testScheduler))
        val signalingUseCase = SendSignalingDataUseCase(repo, scope(testScheduler))
        val useCase = StartCallUseCase(initializeCallUseCase, signalingUseCase, scope(testScheduler))
        var errorCaught: Exception? = null

        useCase.invoke(
            AudioCallSession(sessionId = "session1"),
            onIceCandidateCreated = {},
            onSendCallSession = {},
            onError = { errorCaught = it }
        )
        advanceUntilIdle()

        assertTrue(errorCaught != null)
    }

    @Test
    fun `SendOfferUseCase reports success`() = runTest {
        val repo = FakeCallRepository(sendOfferToFireBaseSuccess = true)
        val initializeCallUseCase = InitializeCallUseCase(repo, scope(testScheduler))
        val useCase = SendOfferUseCase(initializeCallUseCase)
        var result: Boolean? = null

        useCase.invoke("session1") { result = it }
        advanceUntilIdle()

        assertEquals(true, result)
    }

    @Test
    fun `CreateOfferUseCase forwards created offer`() = runTest {
        val repo = FakeCallRepository(createOfferResult = OfferAnswer(sdp = "sdp1", type = "offer", initiator = "u"))
        val initializeCallUseCase = InitializeCallUseCase(repo, scope(testScheduler))
        val useCase = CreateOfferUseCase(initializeCallUseCase)
        var result: OfferAnswer? = null

        useCase.invoke { result = it }

        assertEquals("sdp1", result?.sdp)
    }

    @Test
    fun `SendIceCandidateUseCase delegates to signaling use case`() = runTest {
        val repo = FakeCallRepository(sendIceCandidateToFireBaseSuccess = true)
        val signalingUseCase = SendSignalingDataUseCase(repo, scope(testScheduler))
        val useCase = SendIceCandidateUseCase(signalingUseCase)

        useCase.invoke("session1", IceCandidateData("cand", "mid", 0), "caller")

        assertEquals(Triple("session1", IceCandidateData("cand", "mid", 0), "caller"), repo.sendIceCandidateToFireBaseCalledWith)
    }

    @Test
    fun `ObserveIceCandidateUseCase delegates to signaling use case`() = runTest {
        val repo = FakeCallRepository()
        val signalingUseCase = SendSignalingDataUseCase(repo, scope(testScheduler))
        val useCase = ObserveIceCandidateUseCase(signalingUseCase)

        useCase.invoke("session1")
        advanceUntilIdle()

        assertEquals("session1", repo.observeIceCandidatesFromCalleeCalledWith)
    }

    @Test
    fun `ObserveAnswer notifies onRejectVideoCall when callee rejects`() = runTest {
        val repo = FakeCallRepository(shouldInvokeRejectCallback = true)
        val signalingUseCase = SendSignalingDataUseCase(repo, scope(testScheduler))
        val useCase = ObserveAnswer(signalingUseCase)
        var rejected = false

        useCase.invoke("session1", "caller1") { rejected = true }
        advanceUntilIdle()

        assertTrue(rejected)
    }

    @Test
    fun `ObserveCallStatus notifies onAcceptCall for accepted status`() = runTest {
        val repo = FakeCallRepository(callStatusToEmit = com.minhtu.firesocialmedia.domain.entity.call.CallStatus.ACCEPTED)
        val signalingUseCase = SendSignalingDataUseCase(repo, scope(testScheduler))
        val useCase = ObserveCallStatus(signalingUseCase)
        var accepted = false

        useCase.invoke("session1", onAcceptCall = { accepted = true }, onEndCall = {})
        advanceUntilIdle()

        assertTrue(accepted)
    }

    @Test
    fun `ObserveVideoCall forwards video offer from a different caller`() = runTest {
        val offer = OfferAnswer(sdp = "s", type = "offer", initiator = "otherUser")
        val repo = FakeCallRepository(videoOfferToEmitOnObserve = offer)
        val videoCallUseCase = VideoCallUseCase(repo, scope(testScheduler))
        val useCase = ObserveVideoCall(videoCallUseCase)
        var received: OfferAnswer? = null

        useCase.invoke("session1", "callerId") { received = it }
        advanceUntilIdle()

        assertEquals(offer, received)
    }

    @Test
    fun `EndCallUseCase delegates to ManageCallStateUseCase`() = runTest {
        val repo = FakeCallRepository(deleteCallSessionResult = true)
        val manageCallStateUseCase = ManageCallStateUseCase(repo)
        val useCase = EndCallUseCase(manageCallStateUseCase)

        val result = useCase.invoke("session1")

        assertTrue(result)
        assertEquals("session1", repo.deleteCallSessionCalledWith)
    }
}
