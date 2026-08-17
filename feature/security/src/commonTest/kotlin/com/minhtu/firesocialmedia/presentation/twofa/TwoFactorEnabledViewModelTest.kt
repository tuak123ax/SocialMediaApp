package com.minhtu.firesocialmedia.presentation.twofa

import com.minhtu.firesocialmedia.domain.usecases.settings.CopyUseCase
import com.minhtu.firesocialmedia.domain.usecases.settings.UpdateVerify2FASuccessUseCase
import com.minhtu.firesocialmedia.testutil.FakeSecuritySettingsRepository
import com.minhtu.firesocialmedia.testutil.FakeSettingsRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalCoroutinesApi::class)
class TwoFactorEnabledViewModelTest {

    private fun buildVm(
        securitySettingsRepo: FakeSecuritySettingsRepository = FakeSecuritySettingsRepository(),
        settingsRepo: FakeSettingsRepository = FakeSettingsRepository(),
        scheduler: kotlinx.coroutines.test.TestCoroutineScheduler
    ) = TwoFactorEnabledViewModel(
        CopyUseCase(securitySettingsRepo),
        UpdateVerify2FASuccessUseCase(settingsRepo),
        StandardTestDispatcher(scheduler)
    )

    @Test
    fun `copyToClipboard invokes copy use case with the backup code`() = runTest {
        val securitySettingsRepo = FakeSecuritySettingsRepository()
        val vm = buildVm(securitySettingsRepo, scheduler = testScheduler)
        vm.copyToClipboard("MY-BACKUP-CODE")
        advanceUntilIdle()

        assertEquals("MY-BACKUP-CODE", securitySettingsRepo.copyInvoked)
    }

    @Test
    fun `updateVerify2FASuccess invokes the use case exactly once`() = runTest {
        val settingsRepo = FakeSettingsRepository()
        val vm = buildVm(settingsRepo = settingsRepo, scheduler = testScheduler)
        vm.updateVerify2FASuccess()
        advanceUntilIdle()
        assertEquals(1, settingsRepo.updateVerify2FAInvokedCount)
    }
}
