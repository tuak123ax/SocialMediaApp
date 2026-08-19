package com.minhtu.firesocialmedia.domain.usecases.call

import com.minhtu.firesocialmedia.domain.entity.call.CallType
import com.minhtu.firesocialmedia.domain.entity.call.IceCandidateData
import com.minhtu.firesocialmedia.domain.entity.call.OfferAnswer
import com.minhtu.firesocialmedia.calling.utils.Utils
import com.minhtu.firesocialmedia.testutil.FakeCallRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class InitializeCallUseCaseTest {

    @Test
    fun `initializeCall invokes onInitializeFinished and forwards ice candidates`() = runTest {
        val repo = FakeCallRepository()
        val useCase = InitializeCallUseCase(repo, CoroutineScope(StandardTestDispatcher(testScheduler)))
        var finished = false
        var receivedCandidate: IceCandidateData? = null

        useCase.initializeCall(
            onInitializeFinished = { finished = true },
            onIceCandidateCreated = { receivedCandidate = it }
        )
        advanceUntilIdle()

        assertTrue(finished)
        assertTrue(repo.initializeCalled)

        // Simulate repo emitting an ICE candidate after initialization
        repo.iceCandidateCreatedCallback?.invoke(IceCandidateData("cand", "0", 0))
        advanceUntilIdle()
        assertEquals("cand", receivedCandidate?.candidate)
    }

    @Test
    fun `createVideoOffer sets initiator when currentUserId provided`() = runTest {
        val repo = FakeCallRepository(createVideoOfferResult = OfferAnswer(sdp = "v", type = "offer", initiator = ""))
        val useCase = InitializeCallUseCase(repo, CoroutineScope(StandardTestDispatcher(testScheduler)))
        var received: OfferAnswer? = null

        useCase.createVideoOffer("user1") { received = it }
        advanceUntilIdle()

        assertEquals("user1", received?.initiator)
    }

    @Test
    fun `createVideoOffer keeps initiator empty when currentUserId is null`() = runTest {
        val repo = FakeCallRepository(createVideoOfferResult = OfferAnswer(sdp = "v", type = "offer", initiator = ""))
        val useCase = InitializeCallUseCase(repo, CoroutineScope(StandardTestDispatcher(testScheduler)))
        var received: OfferAnswer? = null

        useCase.createVideoOffer(null) { received = it }
        advanceUntilIdle()

        assertEquals("", received?.initiator)
    }

    @Test
    fun `createAndSendOffer reports success when repository sends successfully`() = runTest {
        val repo = FakeCallRepository(sendOfferToFireBaseSuccess = true)
        val useCase = InitializeCallUseCase(repo, CoroutineScope(StandardTestDispatcher(testScheduler)))
        var success: Boolean? = null

        useCase.createAndSendOffer("session1", object : Utils.Companion.BasicCallBack {
            override fun onSuccess() { success = true }
            override fun onFailure() { success = false }
        })
        advanceUntilIdle()

        assertTrue(success == true)
        assertEquals("session1", repo.sendOfferToFireBaseCalledWith?.first)
    }

    @Test
    fun `createAndSendOffer reports failure when repository fails to send`() = runTest {
        val repo = FakeCallRepository(sendOfferToFireBaseSuccess = false)
        val useCase = InitializeCallUseCase(repo, CoroutineScope(StandardTestDispatcher(testScheduler)))
        var success: Boolean? = null

        useCase.createAndSendOffer("session1", object : Utils.Companion.BasicCallBack {
            override fun onSuccess() { success = true }
            override fun onFailure() { success = false }
        })
        advanceUntilIdle()

        assertFalse(success!!)
    }

    @Test
    fun `createOffer forwards created offer`() = runTest {
        val repo = FakeCallRepository(createOfferResult = OfferAnswer(sdp = "sdp1", type = "offer", initiator = "x"))
        val useCase = InitializeCallUseCase(repo, CoroutineScope(StandardTestDispatcher(testScheduler)))
        var received: OfferAnswer? = null

        useCase.createOffer { received = it }
        advanceUntilIdle()

        assertEquals("sdp1", received?.sdp)
    }

    @Test
    fun `createAndSendAnswer requests video support based on call type and sets initiator`() = runTest {
        val repo = FakeCallRepository(createAnswerResult = OfferAnswer(sdp = "a", type = "answer", initiator = ""))
        val useCase = InitializeCallUseCase(repo, CoroutineScope(StandardTestDispatcher(testScheduler)))
        var success: Boolean? = null

        useCase.createAndSendAnswer("session1", CallType.VIDEO, "callee1", object : Utils.Companion.BasicCallBack {
            override fun onSuccess() { success = true }
            override fun onFailure() { success = false }
        })
        advanceUntilIdle()

        assertTrue(repo.createAnswerCalledWithVideoSupport == true)
        assertEquals("callee1", repo.sendAnswerToFirebaseCalledWith?.second?.initiator)
        assertTrue(success == true)
    }

    @Test
    fun `setRemoteDescription delegates to repository`() = runTest {
        val repo = FakeCallRepository()
        val useCase = InitializeCallUseCase(repo, CoroutineScope(StandardTestDispatcher(testScheduler)))
        val offer = OfferAnswer(sdp = "s", type = "offer", initiator = "u")

        useCase.setRemoteDescription(offer)

        assertEquals(offer, repo.setRemoteDescriptionCalledWith)
    }

    @Test
    fun `addIceCandidates only forwards fully populated candidates`() = runTest {
        val repo = FakeCallRepository()
        val useCase = InitializeCallUseCase(repo, CoroutineScope(StandardTestDispatcher(testScheduler)))
        val candidates = mapOf(
            "0" to IceCandidateData("cand0", "mid0", 0),
            "1" to IceCandidateData(null, "mid1", 1),
            "2" to IceCandidateData("cand2", null, 2),
            "3" to IceCandidateData("cand3", "mid3", null)
        )

        useCase.addIceCandidates(candidates)

        assertEquals(1, repo.addIceCandidateCalls.size)
        assertEquals("cand0", repo.addIceCandidateCalls[0].sdp)
    }
}
