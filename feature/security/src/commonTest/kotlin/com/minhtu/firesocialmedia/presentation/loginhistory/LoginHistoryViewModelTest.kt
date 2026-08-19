package com.minhtu.firesocialmedia.presentation.loginhistory

import com.minhtu.firesocialmedia.domain.entity.settings.SessionItem
import com.minhtu.firesocialmedia.domain.usecases.settings.DeleteLoginSessionUseCase
import com.minhtu.firesocialmedia.domain.usecases.settings.FetchLoginHistoryListUseCase
import com.minhtu.firesocialmedia.domain.usecases.settings.LogoutSessionUseCase
import com.minhtu.firesocialmedia.domain.usecases.settings.UpdateUserTimestampUseCase
import com.minhtu.firesocialmedia.domain.usecases.settings.VerifyCurrentPasswordUseCase
import com.minhtu.firesocialmedia.testutil.FakeSecuritySettingsRepository
import com.minhtu.firesocialmedia.testutil.FakeSettingsRepository
import com.minhtu.firesocialmedia.testutil.makeUserDTO
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class LoginHistoryViewModelTest {

    private fun buildVm(
        securitySettingsRepo: FakeSecuritySettingsRepository = FakeSecuritySettingsRepository(),
        settingsRepo: FakeSettingsRepository = FakeSettingsRepository(),
        scheduler: kotlinx.coroutines.test.TestCoroutineScheduler
    ) = LoginHistoryViewModel(
        FetchLoginHistoryListUseCase(securitySettingsRepo),
        UpdateUserTimestampUseCase(securitySettingsRepo),
        DeleteLoginSessionUseCase(securitySettingsRepo),
        LogoutSessionUseCase(securitySettingsRepo),
        VerifyCurrentPasswordUseCase(settingsRepo),
        StandardTestDispatcher(scheduler)
    )

    @Test
    fun `fetchLoginHistoryList with empty list emits Empty state`() = runTest {
        val securitySettingsRepo = FakeSecuritySettingsRepository().apply { fetchedSessions = emptyList() }
        val vm = buildVm(securitySettingsRepo, scheduler = testScheduler)
        vm.fetchLoginHistoryList("u1")
        advanceUntilIdle()

        assertEquals(LoginHistoryUiState.Empty, vm.loginHistoryUiState.value)
    }

    @Test
    fun `fetchLoginHistoryList with sessions emits Success state`() = runTest {
        val sessions = listOf(SessionItem(sessionId = "s1", deviceName = "iPhone"))
        val securitySettingsRepo = FakeSecuritySettingsRepository().apply { fetchedSessions = sessions }
        val vm = buildVm(securitySettingsRepo, scheduler = testScheduler)
        vm.fetchLoginHistoryList("u1")
        advanceUntilIdle()

        val state = vm.loginHistoryUiState.value
        assertTrue(state is LoginHistoryUiState.Success)
        assertEquals(1, state.data.size)
    }

    @Test
    fun `deleteLoginSession with wrong password sets WRONG_PASSWORD status and does not delete`() = runTest {
        val settingsRepo = FakeSettingsRepository().apply { reAuthResult = false }
        val securitySettingsRepo = FakeSecuritySettingsRepository()
        val vm = buildVm(securitySettingsRepo, settingsRepo, testScheduler)
        vm.deleteLoginSession("u1", "test@example.com", "wrongPass", "s1")
        advanceUntilIdle()

        assertEquals(LogoutSessionStatus.WRONG_PASSWORD, vm.logoutSessionStatus.value)
        assertEquals(null, securitySettingsRepo.lastDeleteSessionArgs)
    }

    @Test
    fun `deleteLoginSession success triggers a refresh of the login history list`() = runTest {
        val sessions = listOf(SessionItem(sessionId = "s2", deviceName = "Android"))
        val settingsRepo = FakeSettingsRepository().apply { reAuthResult = true }
        val securitySettingsRepo = FakeSecuritySettingsRepository().apply {
            deleteSessionResult = true
            fetchedSessions = sessions
        }
        val vm = buildVm(securitySettingsRepo, settingsRepo, testScheduler)
        vm.deleteLoginSession("u1", "test@example.com", "correct", "s2")
        advanceUntilIdle()

        assertTrue(vm.loginHistoryUiState.value is LoginHistoryUiState.Success)
        assertEquals("u1" to "s2", securitySettingsRepo.lastDeleteSessionArgs)
    }

    @Test
    fun `deleteLoginSession failure sets ERROR status`() = runTest {
        val settingsRepo = FakeSettingsRepository().apply { reAuthResult = true }
        val securitySettingsRepo = FakeSecuritySettingsRepository().apply { deleteSessionResult = false }
        val vm = buildVm(securitySettingsRepo, settingsRepo, testScheduler)
        vm.deleteLoginSession("u1", "test@example.com", "correct", "s1")
        advanceUntilIdle()

        assertEquals(LogoutSessionStatus.ERROR, vm.logoutSessionStatus.value)
    }

    @Test
    fun `logoutSession with wrong password sets WRONG_PASSWORD status`() = runTest {
        val settingsRepo = FakeSettingsRepository().apply { reAuthResult = false }
        val vm = buildVm(settingsRepo = settingsRepo, scheduler = testScheduler)
        vm.logoutSession("u1", "test@example.com", "wrongPass", "s1")
        advanceUntilIdle()

        assertEquals(LogoutSessionStatus.WRONG_PASSWORD, vm.logoutSessionStatus.value)
    }

    @Test
    fun `logoutSession failure sets ERROR status`() = runTest {
        val settingsRepo = FakeSettingsRepository().apply { reAuthResult = true }
        val securitySettingsRepo = FakeSecuritySettingsRepository().apply { logoutSessionResult = false }
        val vm = buildVm(securitySettingsRepo, settingsRepo, testScheduler)
        vm.logoutSession("u1", "test@example.com", "correct", "s1")
        advanceUntilIdle()

        assertEquals(LogoutSessionStatus.ERROR, vm.logoutSessionStatus.value)
    }

    @Test
    fun `resetLogoutSessionStatus restores IDLE`() = runTest {
        val settingsRepo = FakeSettingsRepository().apply { reAuthResult = false }
        val vm = buildVm(settingsRepo = settingsRepo, scheduler = testScheduler)
        vm.logoutSession("u1", "test@example.com", "wrongPass", "s1")
        advanceUntilIdle()
        assertEquals(LogoutSessionStatus.WRONG_PASSWORD, vm.logoutSessionStatus.value)

        vm.resetLogoutSessionStatus()
        assertEquals(LogoutSessionStatus.IDLE, vm.logoutSessionStatus.value)
    }

    @Test
    fun `acknowledgeLoginHistory sets SUCCESS on success and updates user timestamp`() = runTest {
        val securitySettingsRepo = FakeSecuritySettingsRepository().apply { updateTimestampResult = true }
        val vm = buildVm(securitySettingsRepo, scheduler = testScheduler)
        val user = makeUserDTO()
        vm.acknowledgeLoginHistory(user)
        advanceUntilIdle()

        assertEquals(AcknowledgeStatus.SUCCESS, vm.acknowledgeStatus.value)
        assertTrue(user.lastTimeAcknowledgedLoginHistory > 0)
    }

    @Test
    fun `acknowledgeLoginHistory sets ERROR on failure`() = runTest {
        val securitySettingsRepo = FakeSecuritySettingsRepository().apply { updateTimestampResult = false }
        val vm = buildVm(securitySettingsRepo, scheduler = testScheduler)
        vm.acknowledgeLoginHistory(makeUserDTO())
        advanceUntilIdle()

        assertEquals(AcknowledgeStatus.ERROR, vm.acknowledgeStatus.value)
    }

    @Test
    fun `resetAcknowledgeStatus restores IDLE`() = runTest {
        val securitySettingsRepo = FakeSecuritySettingsRepository().apply { updateTimestampResult = true }
        val vm = buildVm(securitySettingsRepo, scheduler = testScheduler)
        vm.acknowledgeLoginHistory(makeUserDTO())
        advanceUntilIdle()
        assertEquals(AcknowledgeStatus.SUCCESS, vm.acknowledgeStatus.value)

        vm.resetAcknowledgeStatus()
        assertEquals(AcknowledgeStatus.IDLE, vm.acknowledgeStatus.value)
    }

    @Test
    fun `acknowledgePrivacyRead updates user lastTimeReadPrivacy on success`() = runTest {
        val securitySettingsRepo = FakeSecuritySettingsRepository().apply { updateTimestampResult = true }
        val vm = buildVm(securitySettingsRepo, scheduler = testScheduler)
        val user = makeUserDTO()
        vm.acknowledgePrivacyRead(user)
        advanceUntilIdle()

        assertTrue(user.lastTimeReadPrivacy > 0)
    }

    @Test
    fun `acknowledgePrivacyRead leaves lastTimeReadPrivacy untouched on failure`() = runTest {
        val securitySettingsRepo = FakeSecuritySettingsRepository().apply { updateTimestampResult = false }
        val vm = buildVm(securitySettingsRepo, scheduler = testScheduler)
        val user = makeUserDTO()
        vm.acknowledgePrivacyRead(user)
        advanceUntilIdle()

        assertEquals(0L, user.lastTimeReadPrivacy)
    }
}
