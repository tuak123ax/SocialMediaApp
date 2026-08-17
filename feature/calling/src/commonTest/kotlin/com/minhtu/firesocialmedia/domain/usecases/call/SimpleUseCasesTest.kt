package com.minhtu.firesocialmedia.domain.usecases.call

import com.minhtu.firesocialmedia.calling.entity.user.UserInstance
import com.minhtu.firesocialmedia.domain.entity.call.OfferAnswer
import com.minhtu.firesocialmedia.domain.entity.call.SpeakerType
import com.minhtu.firesocialmedia.testutil.FakeCallRepository
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class RequestCameraAndAudioPermissionsUseCaseTest {
    @Test
    fun `returns granted result from repository`() = runTest {
        val repo = FakeCallRepository(requestCameraAndAudioPermissionsResult = true)
        val useCase = RequestCameraAndAudioPermissionsUseCase(repo)
        assertTrue(useCase.invoke())
    }

    @Test
    fun `returns denied result from repository`() = runTest {
        val repo = FakeCallRepository(requestCameraAndAudioPermissionsResult = false)
        val useCase = RequestCameraAndAudioPermissionsUseCase(repo)
        assertFalse(useCase.invoke())
    }
}

class RequestPermissionUseCaseTest {
    @Test
    fun `returns granted audio permission result`() = runTest {
        val repo = FakeCallRepository(requestAudioPermissionResult = true)
        val useCase = RequestPermissionUseCase(repo)
        assertTrue(useCase.invoke())
    }

    @Test
    fun `returns denied audio permission result`() = runTest {
        val repo = FakeCallRepository(requestAudioPermissionResult = false)
        val useCase = RequestPermissionUseCase(repo)
        assertFalse(useCase.invoke())
    }
}

class StartCallServiceUseCaseTest {
    @Test
    fun `invokes repository startCallService with given args`() = runTest {
        val repo = FakeCallRepository()
        val useCase = StartCallServiceUseCase(repo)
        val caller = UserInstance(uid = "caller1", name = "Caller")
        val callee = UserInstance(uid = "callee1", name = "Callee")

        useCase.invoke("session1", caller, callee)

        assertEquals(Triple("session1", caller, callee), repo.startCallServiceCalledWith)
    }
}

class StartVideoCallServiceUseCaseTest {
    @Test
    fun `invokes repository startVideoCallService with given args`() = runTest {
        val repo = FakeCallRepository()
        val useCase = StartVideoCallServiceUseCase(repo)
        val caller = UserInstance(uid = "caller1", name = "Caller")
        val callee = UserInstance(uid = "callee1", name = "Callee")
        val offer = OfferAnswer(sdp = "s", type = "offer", initiator = "caller1")

        useCase.invoke("session1", caller, callee, "caller1", offer)

        val args = repo.startVideoCallServiceCalledWith
        assertEquals("session1", args?.sessionId)
        assertEquals(caller, args?.caller)
        assertEquals(callee, args?.callee)
        assertEquals("caller1", args?.currentUserId)
        assertEquals(offer, args?.remoteVideoOffer)
    }
}

class StopCallServiceUseCaseTest {
    @Test
    fun `invokes repository stopCallService`() = runTest {
        val repo = FakeCallRepository()
        val useCase = StopCallServiceUseCase(repo)

        useCase.invoke()

        assertTrue(repo.stopCallServiceInvoked)
    }
}

class UpdateCameraStatusUseCaseTest {
    @Test
    fun `delegates camera off status to repository`() = runTest {
        val repo = FakeCallRepository()
        val useCase = UpdateCameraStatusUseCase(repo)

        useCase.invoke(true)

        assertEquals(true, repo.cameraStatus)
    }

    @Test
    fun `delegates camera on status to repository`() = runTest {
        val repo = FakeCallRepository()
        val useCase = UpdateCameraStatusUseCase(repo)

        useCase.invoke(false)

        assertEquals(false, repo.cameraStatus)
    }
}

class UpdateMicStatusUseCaseTest {
    @Test
    fun `delegates mute status to repository`() = runTest {
        val repo = FakeCallRepository()
        val useCase = UpdateMicStatusUseCase(repo)

        useCase.invoke(true)

        assertEquals(true, repo.muteStatus)
    }
}

class UpdateSpeakerStatusUseCaseTest {
    @Test
    fun `delegates speaker status to repository`() = runTest {
        val repo = FakeCallRepository()
        val useCase = UpdateSpeakerStatusUseCase(repo)

        useCase.invoke(SpeakerType.Speaker)

        assertEquals(SpeakerType.Speaker, repo.speakerStatus)
    }

    @Test
    fun `delegates audio speaker status to repository`() = runTest {
        val repo = FakeCallRepository()
        val useCase = UpdateSpeakerStatusUseCase(repo)

        useCase.invoke(SpeakerType.Audio)

        assertEquals(SpeakerType.Audio, repo.speakerStatus)
    }
}
