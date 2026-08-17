package com.minhtu.firesocialmedia.presentation.twofa

import com.minhtu.firesocialmedia.domain.entity.authentication.TwoFAResponse
import com.minhtu.firesocialmedia.domain.usecases.common.security.GetCurrentUserUidUseCase
import com.minhtu.firesocialmedia.domain.usecases.settings.VerifyBackupCodeUseCase
import com.minhtu.firesocialmedia.testutil.FakeSettingsRepository
import com.minhtu.firesocialmedia.testutil.FakeTwoFactorAuthRepository
import com.minhtu.firesocialmedia.testutil.FakeUserRepository
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
class BackUpCodeViewModelTest {

    private fun buildVm(
        userRepo: FakeUserRepository = FakeUserRepository(),
        twoFARepo: FakeTwoFactorAuthRepository = FakeTwoFactorAuthRepository(),
        settingsRepo: FakeSettingsRepository = FakeSettingsRepository(),
        scheduler: kotlinx.coroutines.test.TestCoroutineScheduler
    ) = BackUpCodeViewModel(
        GetCurrentUserUidUseCase(userRepo),
        VerifyBackupCodeUseCase(twoFARepo, settingsRepo),
        StandardTestDispatcher(scheduler)
    )

    @Test
    fun `updateBackupCode updates state`() = runTest {
        val vm = buildVm(scheduler = testScheduler)
        vm.updateBackupCode("ABCDE-12345")
        assertEquals("ABCDE-12345", vm.backupCode.value)
    }

    @Test
    fun `verifyBackupCode with valid userId and correct code sets success result and updates 2FA status`() = runTest {
        val twoFARepo = FakeTwoFactorAuthRepository().apply {
            verifyBackupCodeResult = TwoFAResponse(success = true, message = "ok")
        }
        val settingsRepo = FakeSettingsRepository()
        val vm = buildVm(twoFARepo = twoFARepo, settingsRepo = settingsRepo, scheduler = testScheduler)
        vm.verifyBackupCode("ABCDE12345")
        advanceUntilIdle()

        val result = vm.verifyBackupCodeStatus.value
        assertNotNull(result)
        assertTrue(result.success)
        assertEquals(1, settingsRepo.updateVerify2FAInvokedCount)
    }

    @Test
    fun `verifyBackupCode with wrong code sets failure result and does not update 2FA status`() = runTest {
        val twoFARepo = FakeTwoFactorAuthRepository().apply {
            verifyBackupCodeResult = TwoFAResponse(success = false, message = "invalid code")
        }
        val settingsRepo = FakeSettingsRepository()
        val vm = buildVm(twoFARepo = twoFARepo, settingsRepo = settingsRepo, scheduler = testScheduler)
        vm.verifyBackupCode("WRONGCODE")
        advanceUntilIdle()

        val result = vm.verifyBackupCodeStatus.value
        assertNotNull(result)
        assertFalse(result.success)
        assertEquals(0, settingsRepo.updateVerify2FAInvokedCount)
    }

    @Test
    fun `verifyBackupCode with null userId does not update result`() = runTest {
        val userRepo = FakeUserRepository(currentUid = null)
        val vm = buildVm(userRepo = userRepo, scheduler = testScheduler)
        vm.verifyBackupCode("ABCDE12345")
        advanceUntilIdle()

        assertNull(vm.verifyBackupCodeStatus.value)
    }

    @Test
    fun `resetVerifyBackupCodeStatus clears result`() = runTest {
        val vm = buildVm(scheduler = testScheduler)
        vm.verifyBackupCode("ABCDE12345")
        advanceUntilIdle()
        assertNotNull(vm.verifyBackupCodeStatus.value)

        vm.resetVerifyBackupCodeStatus()
        assertNull(vm.verifyBackupCodeStatus.value)
    }
}
