package com.minhtu.firesocialmedia.domain.usecases.call

import com.minhtu.firesocialmedia.domain.entity.call.CallingRequestData
import com.minhtu.firesocialmedia.domain.entity.call.IceCandidateData
import com.minhtu.firesocialmedia.domain.entity.call.OfferAnswer
import com.minhtu.firesocialmedia.testutil.FakeCallRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class CalleeUseCasesTest {

    @Test
    fun `ListenForIncomingCallsUseCase delegates to InitializeCallUseCase`() = runTest {
        val repo = FakeCallRepository()
        val initializeCallUseCase = InitializeCallUseCase(repo, CoroutineScope(StandardTestDispatcher(testScheduler)))
        val useCase = ListenForIncomingCallsUseCase(initializeCallUseCase)
        var finished = false

        useCase.invoke(onInitializeFinished = { finished = true }, onIceCandidateCreated = {})
        advanceUntilIdle()

        assertTrue(finished)
        assertTrue(repo.initializeCalled)
    }

    @Test
    fun `ObservePhoneCallUseCase forwards request for matching callee`() = runTest {
        val request = CallingRequestData(sessionId = "s1", callerId = "c1", calleeId = "user1")
        val repo = FakeCallRepository(phoneCallRequestToEmit = request)
        val signalingUseCase = SendSignalingDataUseCase(repo, CoroutineScope(StandardTestDispatcher(testScheduler)))
        val useCase = ObservePhoneCallUseCase(signalingUseCase)
        var received: CallingRequestData? = null

        useCase.invoke(
            "user1",
            onReceivePhoneCallRequest = { received = it },
            iceCandidateCallBack = {},
            onEndCall = {},
            whoEndCallCallBack = {}
        )
        advanceUntilIdle()

        assertEquals(request, received)
    }

    @Test
    fun `ObservePhoneCallWithInCallUseCase forwards request for matching callee`() = runTest {
        val request = CallingRequestData(sessionId = "s1", callerId = "c1", calleeId = "user1")
        val repo = FakeCallRepository(phoneCallRequestToEmit = request)
        val signalingUseCase = SendSignalingDataUseCase(repo, CoroutineScope(StandardTestDispatcher(testScheduler)))
        val useCase = ObservePhoneCallWithInCallUseCase(signalingUseCase)
        var received: CallingRequestData? = null

        useCase.invoke(
            kotlinx.coroutines.flow.MutableStateFlow(false),
            "user1",
            onReceivePhoneCallRequest = { received = it },
            onEndCall = {},
            whoEndCallCallBack = {}
        )
        advanceUntilIdle()

        assertEquals(request, received)
    }

    @Test
    fun `StopObservePhoneCallUseCase delegates to repository`() = runTest {
        val repo = FakeCallRepository()
        val signalingUseCase = SendSignalingDataUseCase(repo, CoroutineScope(StandardTestDispatcher(testScheduler)))
        val useCase = StopObservePhoneCallUseCase(signalingUseCase)

        useCase.invoke()

        assertTrue(repo.stopObservePhoneCallInvoked)
    }

    @Test
    fun `SetRemoteDescriptionUseCase delegates to InitializeCallUseCase`() = runTest {
        val repo = FakeCallRepository()
        val initializeCallUseCase = InitializeCallUseCase(repo, CoroutineScope(StandardTestDispatcher(testScheduler)))
        val useCase = SetRemoteDescriptionUseCase(initializeCallUseCase)
        val offer = OfferAnswer(sdp = "s", type = "offer", initiator = "u")

        useCase.invoke(offer)

        assertEquals(offer, repo.setRemoteDescriptionCalledWith)
    }

    @Test
    fun `SendAnswerUseCase creates and sends answer using detected call type`() = runTest {
        val repo = FakeCallRepository(sendAnswerToFirebaseSuccess = true)
        val initializeCallUseCase = InitializeCallUseCase(repo, CoroutineScope(StandardTestDispatcher(testScheduler)))
        val useCase = SendAnswerUseCase(initializeCallUseCase)
        var result: Boolean? = null

        useCase.invoke("session1", OfferAnswer(sdp = "m=video", type = "offer", initiator = "u")) { result = it }
        advanceUntilIdle()

        assertTrue(result == true)
        assertTrue(repo.createAnswerCalledWithVideoSupport == true)
    }

    @Test
    fun `AcceptCallUseCase delegates to ManageCallStateUseCase`() = runTest {
        val repo = FakeCallRepository(sendCallStatusToFirebaseResult = true)
        val manageCallStateUseCase = ManageCallStateUseCase(repo)
        val useCase = AcceptCallUseCase(manageCallStateUseCase)

        val result = useCase.invoke("session1")

        assertTrue(result)
    }

    @Test
    fun `AddIceCandidatesUseCase forwards candidates via InitializeCallUseCase`() = runTest {
        val repo = FakeCallRepository()
        val initializeCallUseCase = InitializeCallUseCase(repo, CoroutineScope(StandardTestDispatcher(testScheduler)))
        val useCase = AddIceCandidatesUseCase(initializeCallUseCase)

        useCase.invoke(mapOf("0" to IceCandidateData("cand", "mid", 0)))

        assertEquals(1, repo.addIceCandidateCalls.size)
    }

    @Test
    fun `SendWhoEndCallUseCase delegates to ManageCallStateUseCase`() = runTest {
        val repo = FakeCallRepository(sendWhoEndCallResult = true)
        val manageCallStateUseCase = ManageCallStateUseCase(repo)
        val useCase = SendWhoEndCallUseCase(manageCallStateUseCase)

        val result = useCase.invoke("session1", "user1")

        assertTrue(result)
        assertEquals("session1" to "user1", repo.sendWhoEndCallCalledWith)
    }
}
