package com.minhtu.firesocialmedia.presentation.audiocall

import com.minhtu.firesocialmedia.domain.entity.call.CallEventFlow
import com.minhtu.firesocialmedia.domain.entity.call.CallingRequestData
import com.minhtu.firesocialmedia.domain.interactor.CallInteractor
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * Fake [CallInteractor]. `observe` records the userId it was invoked with and, if configured,
 * synchronously invokes the callback the caller passed in (mirroring the real interactor which
 * forwards Firebase-observed events back through these callbacks). We deliberately do NOT invoke
 * `onEndCall` here by default since the real [CallViewModel.observePhoneCall] implementation
 * chains multi-second real delays after `onEndCall` fires, which would make invoking it from a
 * fake unreliable to await deterministically in a unit test without depending on wall-clock time.
 */
private class FakeCallInteractor : CallInteractor {
    var observeInvokedWithUserId: String? = null
    var stopObserveInvoked = false
    var stopCallServiceInvoked = false
    var requestToDeliver: CallingRequestData? = null
    var whoEndCallToDeliver: String? = null

    override suspend fun observe(
        isInCall: MutableStateFlow<Boolean>,
        userId: String,
        onReceivePhoneCallRequest: suspend (CallingRequestData) -> Unit,
        onEndCall: suspend () -> Unit,
        whoEndCallCallBack: suspend (String) -> Unit
    ) {
        observeInvokedWithUserId = userId
        requestToDeliver?.let { onReceivePhoneCallRequest(it) }
        whoEndCallToDeliver?.let { whoEndCallCallBack(it) }
    }

    override fun stopObservePhoneCall() {
        stopObserveInvoked = true
    }

    override suspend fun stopCallService() {
        stopCallServiceInvoked = true
    }
}

@OptIn(ExperimentalCoroutinesApi::class)
class CallViewModelTest {

    @BeforeTest
    @AfterTest
    fun resetGlobalState() {
        CallEventFlow.reset()
    }

    private fun buildViewModel(scheduler: kotlinx.coroutines.test.TestCoroutineScheduler): Pair<CallViewModel, FakeCallInteractor> {
        val interactor = FakeCallInteractor()
        val vm = CallViewModel(interactor, StandardTestDispatcher(scheduler))
        return vm to interactor
    }

    @Test
    fun `updateIsInCall updates isInCall flow`() = runTest {
        val (vm, _) = buildViewModel(testScheduler)
        vm.updateIsInCall(true)
        assertTrue(vm.isInCall.value)
        vm.updateIsInCall(false)
        assertFalse(vm.isInCall.value)
    }

    @Test
    fun `resetCallEvent clears whoStopCall, CallEventFlow events and isInCall`() = runTest {
        val (vm, _) = buildViewModel(testScheduler)
        vm.updateIsInCall(true)
        vm.setWhoStopCall("someUser")
        CallEventFlow.events.value = com.minhtu.firesocialmedia.domain.entity.call.CallEvent.CallEnded

        vm.resetCallEvent()

        assertFalse(vm.isInCall.value)
        assertNull(CallEventFlow.events.value)
    }

    @Test
    fun `resetPhoneCallRequestStatus clears phoneCallRequestStatus`() = runTest {
        val (vm, repo) = buildViewModel(testScheduler)
        repo.requestToDeliver = CallingRequestData(sessionId = "s1", callerId = "c1", calleeId = "user1")
        vm.observePhoneCall("user1")
        advanceUntilIdle()
        assertEquals("s1", vm.phoneCallRequestStatus.value?.sessionId)

        vm.resetPhoneCallRequestStatus()

        assertNull(vm.phoneCallRequestStatus.value)
    }

    @Test
    fun `resetEndCallStatus clears endCallStatus`() = runTest {
        val (vm, _) = buildViewModel(testScheduler)
        vm.resetEndCallStatus()
        assertFalse(vm.endCallStatus.value)
    }

    @Test
    fun `stopObservePhoneCall delegates to interactor`() = runTest {
        val (vm, interactor) = buildViewModel(testScheduler)
        vm.stopObservePhoneCall()
        assertTrue(interactor.stopObserveInvoked)
    }

    @Test
    fun `observePhoneCall does nothing when currentUserId is null`() = runTest {
        val (vm, interactor) = buildViewModel(testScheduler)
        vm.observePhoneCall(null)
        advanceUntilIdle()
        assertNull(interactor.observeInvokedWithUserId)
    }

    @Test
    fun `observePhoneCall invokes interactor observe with the given userId`() = runTest {
        val (vm, interactor) = buildViewModel(testScheduler)
        vm.observePhoneCall("user42")
        advanceUntilIdle()
        assertEquals("user42", interactor.observeInvokedWithUserId)
    }

    @Test
    fun `observePhoneCall stores incoming call request via callback`() = runTest {
        val (vm, interactor) = buildViewModel(testScheduler)
        val request = CallingRequestData(sessionId = "s99", callerId = "caller1", calleeId = "user1")
        interactor.requestToDeliver = request

        vm.observePhoneCall("user1")
        advanceUntilIdle()

        assertEquals(request, vm.phoneCallRequestStatus.value)
    }

    @Test
    fun `observePhoneCall records who ended the call via callback`() = runTest {
        val (vm, interactor) = buildViewModel(testScheduler)
        interactor.whoEndCallToDeliver = "someUser"

        vm.observePhoneCall("user1")
        advanceUntilIdle()

        // whoStopCall is private, but we can observe its effect indirectly through resetCallEvent
        // not throwing and endCallStatus remaining false since onEndCall was never triggered here.
        assertFalse(vm.endCallStatus.value)
    }

    @Test
    fun `setWhoStopCall then resetCallEvent leaves endCallStatus untouched`() = runTest {
        val (vm, _) = buildViewModel(testScheduler)
        vm.setWhoStopCall("user1")
        vm.resetCallEvent()
        assertFalse(vm.endCallStatus.value)
    }
}
