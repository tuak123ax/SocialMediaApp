package com.minhtu.firesocialmedia.presentation.settings

import com.minhtu.firesocialmedia.domain.entity.authentication.TwoFAResponse
import com.minhtu.firesocialmedia.domain.usecases.settings.Disable2FAUseCase
import com.minhtu.firesocialmedia.testutil.FakeSettingsRepository
import com.minhtu.firesocialmedia.testutil.FakeTwoFactorAuthRepository
import com.minhtu.firesocialmedia.testutil.makeUserDTO
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class SecuritySettingsViewModelTest {

    private fun buildVm(
        twoFARepo: FakeTwoFactorAuthRepository = FakeTwoFactorAuthRepository(),
        settingsRepo: FakeSettingsRepository = FakeSettingsRepository(),
        scheduler: kotlinx.coroutines.test.TestCoroutineScheduler
    ) = SecuritySettingsViewModel(
        Disable2FAUseCase(twoFARepo, settingsRepo),
        StandardTestDispatcher(scheduler)
    )

    @Test
    fun `disable2FA emits success response and clears local 2FA status`() = runTest {
        val twoFARepo = FakeTwoFactorAuthRepository().apply {
            disable2FAResult = TwoFAResponse(success = true, message = "ok")
        }
        val settingsRepo = FakeSettingsRepository()
        val vm = buildVm(twoFARepo, settingsRepo, testScheduler)

        vm.disable2FA(makeUserDTO(twoFAEnabled = true))
        advanceUntilIdle()

        val result = vm.disable2FAStatus.value
        assertNotNull(result)
        assertTrue(result.success)
        assertEquals(1, settingsRepo.delete2FAStatusInLocalInvokedCount)
        assertEquals(false, twoFARepo.lastUpdateFlagArgs?.second)
    }

    @Test
    fun `disable2FA emits failure when repo fails and does not touch flag or local status`() = runTest {
        val twoFARepo = FakeTwoFactorAuthRepository().apply {
            disable2FAResult = TwoFAResponse(success = false, message = "error")
        }
        val settingsRepo = FakeSettingsRepository()
        val vm = buildVm(twoFARepo, settingsRepo, testScheduler)

        vm.disable2FA(makeUserDTO())
        advanceUntilIdle()

        val result = vm.disable2FAStatus.value
        assertNotNull(result)
        assertFalse(result.success)
        assertEquals(0, settingsRepo.delete2FAStatusInLocalInvokedCount)
        assertEquals(0, twoFARepo.updateFlagInvokedCount)
    }

    @Test
    fun `resetDisable2FAStatus clears the state`() = runTest {
        val vm = buildVm(scheduler = testScheduler)
        vm.disable2FA(makeUserDTO())
        advanceUntilIdle()
        assertNotNull(vm.disable2FAStatus.value)

        vm.resetDisable2FAStatus()

        assertNull(vm.disable2FAStatus.value)
    }
}
