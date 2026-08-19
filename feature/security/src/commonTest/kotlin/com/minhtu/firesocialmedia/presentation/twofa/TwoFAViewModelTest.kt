package com.minhtu.firesocialmedia.presentation.twofa

import com.minhtu.firesocialmedia.domain.usecases.settings.BuildOtpAuthUrlUseCase
import com.minhtu.firesocialmedia.domain.usecases.settings.CopyUseCase
import com.minhtu.firesocialmedia.domain.usecases.settings.GenerateSecretFor2FAUseCase
import com.minhtu.firesocialmedia.testutil.FakeSecuritySettingsRepository
import com.minhtu.firesocialmedia.testutil.FakeTwoFactorAuthRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class TwoFAViewModelTest {

    private fun buildVm(
        twoFARepo: FakeTwoFactorAuthRepository = FakeTwoFactorAuthRepository(),
        securitySettingsRepo: FakeSecuritySettingsRepository = FakeSecuritySettingsRepository(),
        scheduler: kotlinx.coroutines.test.TestCoroutineScheduler
    ) = TwoFAViewModel(
        GenerateSecretFor2FAUseCase(twoFARepo),
        BuildOtpAuthUrlUseCase(),
        CopyUseCase(securitySettingsRepo),
        StandardTestDispatcher(scheduler)
    )

    @Test
    fun `generateSecretFor2FA populates secretFor2FA`() = runTest {
        val twoFARepo = FakeTwoFactorAuthRepository().apply { generatedSecret = "ABCDEFGH1234" }
        val vm = buildVm(twoFARepo, scheduler = testScheduler)
        vm.generateSecretFor2FA()
        advanceUntilIdle()

        assertNotNull(vm.secretFor2FA.value)
        assertEquals("ABCDEFGH1234", vm.secretFor2FA.value)
    }

    @Test
    fun `buildOtpAuthUrl returns a well formed otpauth url`() = runTest {
        val vm = buildVm(scheduler = testScheduler)
        val url = vm.buildOtpAuthUrl("MyApp", "user@test.com", "SECRET")
        assertTrue(url.isNotEmpty())
        assertTrue(url.startsWith("otpauth://totp/MyApp:user@test.com"))
        assertTrue(url.contains("secret=SECRET"))
        assertTrue(url.contains("issuer=MyApp"))
    }

    @Test
    fun `buildOtpAuthUrl handles null secret gracefully`() = runTest {
        val vm = buildVm(scheduler = testScheduler)
        val url = vm.buildOtpAuthUrl("MyApp", "user@test.com", null)
        assertTrue(url.contains("secret=null"))
    }

    @Test
    fun `copySecret invokes copy use case`() = runTest {
        val securitySettingsRepo = FakeSecuritySettingsRepository()
        val vm = buildVm(securitySettingsRepo = securitySettingsRepo, scheduler = testScheduler)
        vm.copySecret("MY_SECRET_KEY")
        advanceUntilIdle()

        assertEquals("MY_SECRET_KEY", securitySettingsRepo.copyInvoked)
    }
}
