package com.minhtu.firesocialmedia.presentation.twofa

import com.minhtu.firesocialmedia.domain.entity.authentication.TwoFAResponse
import com.minhtu.firesocialmedia.domain.usecases.home.security.ClearAccountUseCase
import com.minhtu.firesocialmedia.domain.usecases.settings.Enable2FAUseCase
import com.minhtu.firesocialmedia.domain.usecases.settings.Verify2FAUseCase
import com.minhtu.firesocialmedia.testutil.FakeSecurityCryptoService
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
class VerifyOTPViewModelTest {

    private fun buildVm(
        twoFARepo: FakeTwoFactorAuthRepository = FakeTwoFactorAuthRepository(),
        settingsRepo: FakeSettingsRepository = FakeSettingsRepository(),
        crypto: FakeSecurityCryptoService = FakeSecurityCryptoService(),
        scheduler: kotlinx.coroutines.test.TestCoroutineScheduler
    ) = VerifyOTPViewModel(
        Enable2FAUseCase(twoFARepo),
        Verify2FAUseCase(twoFARepo, settingsRepo),
        ClearAccountUseCase(crypto),
        StandardTestDispatcher(scheduler)
    )

    @Test
    fun `updateOtpToVerify updates state for valid length`() = runTest {
        val vm = buildVm(scheduler = testScheduler)
        vm.updateOtpToVerify("123456")
        assertEquals("123456", vm.otpToVerify.value)
    }

    @Test
    fun `updateOtpToVerify ignores input longer than 6 digits`() = runTest {
        val vm = buildVm(scheduler = testScheduler)
        vm.updateOtpToVerify("1234567")
        assertEquals("", vm.otpToVerify.value)
    }

    @Test
    fun `enableOTP with correct otp sets success result`() = runTest {
        val twoFARepo = FakeTwoFactorAuthRepository().apply {
            enableOTPResult = TwoFAResponse(success = true, message = "ok")
        }
        val vm = buildVm(twoFARepo, scheduler = testScheduler)
        vm.enableOTP(makeUserDTO(), "SECRET", "123456")
        advanceUntilIdle()

        val result = vm.verifyOTPResult.value
        assertNotNull(result)
        assertTrue(result.success)
        assertEquals(true, twoFARepo.lastUpdateFlagArgs?.second)
    }

    @Test
    fun `enableOTP with wrong otp sets failure result and does not flip twoFA flag`() = runTest {
        val twoFARepo = FakeTwoFactorAuthRepository().apply {
            enableOTPResult = TwoFAResponse(success = false, message = "invalid otp")
        }
        val vm = buildVm(twoFARepo, scheduler = testScheduler)
        vm.enableOTP(makeUserDTO(), "SECRET", "000000")
        advanceUntilIdle()

        val result = vm.verifyOTPResult.value
        assertNotNull(result)
        assertFalse(result.success)
        assertEquals(0, twoFARepo.updateFlagInvokedCount)
    }

    @Test
    fun `verifyOTP with correct otp sets success result and updates 2FA verified status`() = runTest {
        val twoFARepo = FakeTwoFactorAuthRepository().apply {
            verifyOTPResult = TwoFAResponse(success = true, message = "ok")
        }
        val settingsRepo = FakeSettingsRepository()
        val vm = buildVm(twoFARepo, settingsRepo, scheduler = testScheduler)
        vm.verifyOTP(makeUserDTO(), "123456")
        advanceUntilIdle()

        val result = vm.verifyOTPResult.value
        assertNotNull(result)
        assertTrue(result.success)
        assertEquals(1, settingsRepo.updateVerify2FAInvokedCount)
    }

    @Test
    fun `verifyOTP with wrong otp sets failure result and does not update 2FA verified status`() = runTest {
        val twoFARepo = FakeTwoFactorAuthRepository().apply {
            verifyOTPResult = TwoFAResponse(success = false, message = "invalid otp")
        }
        val settingsRepo = FakeSettingsRepository()
        val vm = buildVm(twoFARepo, settingsRepo, scheduler = testScheduler)
        vm.verifyOTP(makeUserDTO(), "000000")
        advanceUntilIdle()

        val result = vm.verifyOTPResult.value
        assertNotNull(result)
        assertFalse(result.success)
        assertEquals(0, settingsRepo.updateVerify2FAInvokedCount)
    }

    @Test
    fun `resetVerifyOTPResult clears result`() = runTest {
        val vm = buildVm(scheduler = testScheduler)
        vm.verifyOTP(makeUserDTO(), "123456")
        advanceUntilIdle()
        assertNotNull(vm.verifyOTPResult.value)

        vm.resetVerifyOTPResult()
        assertNull(vm.verifyOTPResult.value)
    }

    @Test
    fun `resetOtp clears otp field`() = runTest {
        val vm = buildVm(scheduler = testScheduler)
        vm.updateOtpToVerify("654321")
        assertEquals("654321", vm.otpToVerify.value)
        vm.resetOtp()
        assertEquals("", vm.otpToVerify.value)
    }

    @Test
    fun `clearAccountInLocalData invokes clearAccount on crypto service`() = runTest {
        val crypto = FakeSecurityCryptoService()
        val vm = buildVm(crypto = crypto, scheduler = testScheduler)
        vm.clearAccountInLocalData()
        advanceUntilIdle()
        assertEquals(1, crypto.clearAccountInvokedCount)
    }
}
