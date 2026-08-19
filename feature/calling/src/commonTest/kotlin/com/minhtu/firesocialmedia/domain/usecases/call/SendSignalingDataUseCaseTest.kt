package com.minhtu.firesocialmedia.domain.usecases.call

import com.minhtu.firesocialmedia.domain.entity.call.AudioCallSession
import com.minhtu.firesocialmedia.domain.entity.call.CallStatus
import com.minhtu.firesocialmedia.domain.entity.call.CallingRequestData
import com.minhtu.firesocialmedia.domain.entity.call.IceCandidateData
import com.minhtu.firesocialmedia.domain.entity.call.OfferAnswer
import com.minhtu.firesocialmedia.calling.utils.Utils
import com.minhtu.firesocialmedia.testutil.FakeCallRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class SendSignalingDataUseCaseTest {

    private fun useCase(repo: FakeCallRepository, scheduler: kotlinx.coroutines.test.TestCoroutineScheduler) =
        SendSignalingDataUseCase(repo, CoroutineScope(StandardTestDispatcher(scheduler)))

    @Test
    fun `sendCallSessionToFirebase reports success`() = runTest {
        val repo = FakeCallRepository(sendCallSessionToFirebaseSuccess = true)
        val useCase = useCase(repo, testScheduler)
        var success: Boolean? = null

        useCase.sendCallSessionToFirebase(AudioCallSession(sessionId = "s1"), object : Utils.Companion.BasicCallBack {
            override fun onSuccess() { success = true }
            override fun onFailure() { success = false }
        })

        assertTrue(success == true)
        assertEquals("s1", repo.sendCallSessionToFirebaseCalledWith?.sessionId)
    }

    @Test
    fun `observeIceCandidateFromCallee adds fully populated candidates to peer connection`() = runTest {
        val repo = FakeCallRepository(
            iceCandidatesToEmitOnObserve = listOf(
                IceCandidateData("cand1", "mid1", 1),
                IceCandidateData(null, "mid2", 2)
            )
        )
        val useCase = useCase(repo, testScheduler)

        useCase.observeIceCandidateFromCallee("session1")
        advanceUntilIdle()

        assertEquals("session1", repo.observeIceCandidatesFromCalleeCalledWith)
        assertEquals(1, repo.addIceCandidateCalls.size)
        assertEquals("cand1", repo.addIceCandidateCalls[0].sdp)
    }

    @Test
    fun `observeAnswerFromCallee ignores answer authored by given callerId`() = runTest {
        val answer = OfferAnswer(sdp = "a", type = "answer", initiator = "caller1")
        val repo = FakeCallRepository(answerToEmitOnObserve = answer)
        val useCase = useCase(repo, testScheduler)
        var gotAnswer = false

        useCase.observeAnswerFromCallee("session1", callerId = "caller1", onGetAnswerFromCallee = { gotAnswer = true }, onRejectVideoCall = {})
        advanceUntilIdle()

        assertFalse(gotAnswer)
        assertNull(repo.setRemoteDescriptionCalledWith)
    }

    @Test
    fun `observeAnswerFromCallee accepts answer from a different initiator`() = runTest {
        val answer = OfferAnswer(sdp = "a", type = "answer", initiator = "callee1")
        val repo = FakeCallRepository(answerToEmitOnObserve = answer)
        val useCase = useCase(repo, testScheduler)
        var gotAnswer = false

        useCase.observeAnswerFromCallee("session1", callerId = "caller1", onGetAnswerFromCallee = { gotAnswer = true }, onRejectVideoCall = {})
        advanceUntilIdle()

        assertTrue(gotAnswer)
        assertEquals(answer, repo.setRemoteDescriptionCalledWith)
    }

    @Test
    fun `observeAnswerFromCallee ignores stale audio answer when expecting video answer`() = runTest {
        val audioAnswer = OfferAnswer(sdp = "m=audio", type = "answer", initiator = "callee1")
        val repo = FakeCallRepository(answerToEmitOnObserve = audioAnswer)
        val useCase = useCase(repo, testScheduler)
        var gotAnswer = false

        useCase.observeAnswerFromCallee(
            "session1", callerId = "caller1", expectVideoAnswer = true,
            onGetAnswerFromCallee = { gotAnswer = true }, onRejectVideoCall = {}
        )
        advanceUntilIdle()

        assertFalse(gotAnswer)
    }

    @Test
    fun `observeAnswerFromCallee rejectCallBack updates offer and notifies reject`() = runTest {
        val repo = FakeCallRepository(shouldInvokeRejectCallback = true, updateOfferInFirebaseSuccess = true)
        val useCase = useCase(repo, testScheduler)
        var rejected = false

        useCase.observeAnswerFromCallee("session1", callerId = "caller1", onGetAnswerFromCallee = {}, onRejectVideoCall = { rejected = true })
        advanceUntilIdle()

        assertTrue(rejected)
        assertEquals(Triple("session1", "", "initiator"), repo.updateOfferInFirebaseCalledWith)
    }

    @Test
    fun `observeCallStatus notifies onAcceptCall for ACCEPTED status`() = runTest {
        val repo = FakeCallRepository(callStatusToEmit = CallStatus.ACCEPTED)
        val useCase = useCase(repo, testScheduler)
        var accepted = false
        var ended = false

        useCase.observeCallStatus("session1", onAcceptCall = { accepted = true }, onEndCall = { ended = true })
        advanceUntilIdle()

        assertTrue(accepted)
        assertFalse(ended)
    }

    @Test
    fun `observeCallStatus does not notify onAcceptCall for other statuses`() = runTest {
        val repo = FakeCallRepository(callStatusToEmit = CallStatus.RINGING)
        val useCase = useCase(repo, testScheduler)
        var accepted = false

        useCase.observeCallStatus("session1", onAcceptCall = { accepted = true }, onEndCall = {})
        advanceUntilIdle()

        assertFalse(accepted)
    }

    @Test
    fun `observeCallStatus notifies onEndCall on failure`() = runTest {
        val repo = FakeCallRepository(shouldInvokeCallStatusFailure = true)
        val useCase = useCase(repo, testScheduler)
        var ended = false

        useCase.observeCallStatus("session1", onAcceptCall = {}, onEndCall = { ended = true })
        advanceUntilIdle()

        assertTrue(ended)
    }

    @Test
    fun `updateAnswerInFirebase sends Reject initiator`() = runTest {
        val repo = FakeCallRepository()
        val useCase = useCase(repo, testScheduler)

        useCase.updateAnswerInFirebase("session1")

        assertEquals(Triple("session1", "Reject", "initiator"), repo.updateAnswerInFirebaseCalledWith)
    }

    @Test
    fun `clearAnswerInFirebaseForNewVideoOffer delegates to repository`() = runTest {
        val repo = FakeCallRepository()
        val useCase = useCase(repo, testScheduler)

        useCase.clearAnswerInFirebaseForNewVideoOffer("session1")

        assertEquals("session1", repo.clearAnswerInFirebaseCalledWith)
    }

    @Test
    fun `sendOfferToFireBase delegates to repository`() = runTest {
        val repo = FakeCallRepository()
        val useCase = useCase(repo, testScheduler)
        val offer = OfferAnswer(sdp = "s", type = "offer", initiator = "u")

        useCase.sendOfferToFireBase("session1", offer)

        assertEquals("session1" to offer, repo.sendOfferToFireBaseCalledWith)
    }

    @Test
    fun `observePhoneCallWithoutCheckingInCall forwards request only when calleeId matches`() = runTest {
        val matchingRequest = CallingRequestData(sessionId = "s1", callerId = "c1", calleeId = "user1")
        val repo = FakeCallRepository(phoneCallRequestToEmit = matchingRequest)
        val useCase = useCase(repo, testScheduler)
        var received: CallingRequestData? = null

        useCase.observePhoneCallWithoutCheckingInCall(
            "user1",
            onReceivePhoneCallRequest = { received = it },
            iceCandidateCallBack = {},
            onEndCall = {},
            whoEndCallCallBack = {}
        )
        advanceUntilIdle()

        assertEquals(matchingRequest, received)
    }

    @Test
    fun `observePhoneCallWithoutCheckingInCall ignores request for a different callee`() = runTest {
        val otherRequest = CallingRequestData(sessionId = "s1", callerId = "c1", calleeId = "someoneElse")
        val repo = FakeCallRepository(phoneCallRequestToEmit = otherRequest)
        val useCase = useCase(repo, testScheduler)
        var received: CallingRequestData? = null

        useCase.observePhoneCallWithoutCheckingInCall(
            "user1",
            onReceivePhoneCallRequest = { received = it },
            iceCandidateCallBack = {},
            onEndCall = {},
            whoEndCallCallBack = {}
        )
        advanceUntilIdle()

        assertNull(received)
    }

    @Test
    fun `observePhoneCallWithoutCheckingInCall invokes onEndCall when end flag true`() = runTest {
        val repo = FakeCallRepository(endCallSessionToEmit = true)
        val useCase = useCase(repo, testScheduler)
        var ended = false

        useCase.observePhoneCallWithoutCheckingInCall(
            "user1",
            onReceivePhoneCallRequest = {},
            iceCandidateCallBack = {},
            onEndCall = { ended = true },
            whoEndCallCallBack = {}
        )
        advanceUntilIdle()

        assertTrue(ended)
    }

    @Test
    fun `observePhoneCallWithCheckingInCall forwards request only when calleeId matches`() = runTest {
        val matchingRequest = CallingRequestData(sessionId = "s1", callerId = "c1", calleeId = "user1")
        val repo = FakeCallRepository(phoneCallRequestToEmit = matchingRequest)
        val useCase = useCase(repo, testScheduler)
        var received: CallingRequestData? = null

        useCase.observePhoneCallWithCheckingInCall(
            MutableStateFlow(false),
            "user1",
            onReceivePhoneCallRequest = { received = it },
            onEndCall = {},
            whoEndCallCallBack = {}
        )
        advanceUntilIdle()

        assertEquals(matchingRequest, received)
    }

    @Test
    fun `sendAnswerToFirebase delegates to repository`() = runTest {
        val repo = FakeCallRepository()
        val useCase = useCase(repo, testScheduler)
        val answer = OfferAnswer(sdp = "a", type = "answer", initiator = "u")

        useCase.sendAnswerToFirebase("session1", answer)

        assertEquals("session1" to answer, repo.sendAnswerToFirebaseCalledWith)
    }

    @Test
    fun `sendIceCandidateToFireBase delegates to repository`() = runTest {
        val repo = FakeCallRepository(sendIceCandidateToFireBaseSuccess = true)
        val useCase = useCase(repo, testScheduler)
        var success: Boolean? = null

        useCase.sendIceCandidateToFireBase(
            "session1", IceCandidateData("cand", "mid", 0), "caller",
            object : Utils.Companion.BasicCallBack {
                override fun onSuccess() { success = true }
                override fun onFailure() { success = false }
            }
        )

        assertTrue(success == true)
    }

    @Test
    fun `stopObservePhoneCall delegates to repository`() = runTest {
        val repo = FakeCallRepository()
        val useCase = useCase(repo, testScheduler)

        useCase.stopObservePhoneCall()

        assertTrue(repo.stopObservePhoneCallInvoked)
    }
}
