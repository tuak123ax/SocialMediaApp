package com.minhtu.firesocialmedia.domain.usecases.call

import com.minhtu.firesocialmedia.domain.entity.call.OfferAnswer
import com.minhtu.firesocialmedia.testutil.FakeCallRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class VideoCallUseCaseTest {

    @Test
    fun `startVideoCall forwards local video track from repository`() = runTest {
        val repo = FakeCallRepository(localVideoTrackToEmit = "track1")
        val useCase = VideoCallUseCase(repo, CoroutineScope(StandardTestDispatcher(testScheduler)))
        var received: Any? = null

        useCase.startVideoCall(isVideoInitiator = true) { received = it }

        assertEquals(true, repo.startVideoCallCalledWithInitiator)
        assertEquals("track1", received)
    }

    @Test
    fun `observeVideoCall forwards offer when initiator differs from caller`() = runTest {
        val offer = OfferAnswer(sdp = "s", type = "offer", initiator = "otherUser")
        val repo = FakeCallRepository(videoOfferToEmitOnObserve = offer)
        val useCase = VideoCallUseCase(repo, CoroutineScope(StandardTestDispatcher(testScheduler)))
        var received: OfferAnswer? = null

        useCase.observeVideoCall("session1", "callerId") { received = it }
        advanceUntilIdle()

        assertEquals(offer, received)
        assertEquals("session1", repo.observeVideoCallCalledWith)
    }

    @Test
    fun `observeVideoCall ignores offer authored by the caller itself`() = runTest {
        val offer = OfferAnswer(sdp = "s", type = "offer", initiator = "callerId")
        val repo = FakeCallRepository(videoOfferToEmitOnObserve = offer)
        val useCase = VideoCallUseCase(repo, CoroutineScope(StandardTestDispatcher(testScheduler)))
        var received: OfferAnswer? = null

        useCase.observeVideoCall("session1", "callerId") { received = it }
        advanceUntilIdle()

        assertNull(received)
    }
}
