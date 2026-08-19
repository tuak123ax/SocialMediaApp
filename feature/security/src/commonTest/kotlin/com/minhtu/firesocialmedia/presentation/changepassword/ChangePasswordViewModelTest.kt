package com.minhtu.firesocialmedia.presentation.changepassword

import com.minhtu.firesocialmedia.domain.entity.settings.ChangePasswordState
import com.minhtu.firesocialmedia.domain.error.changepassword.ChangePasswordError
import com.minhtu.firesocialmedia.domain.usecases.settings.ChangePasswordUseCase
import com.minhtu.firesocialmedia.domain.usecases.settings.ValidateNewPasswordUseCase
import com.minhtu.firesocialmedia.domain.usecases.settings.VerifyCurrentPasswordUseCase
import com.minhtu.firesocialmedia.testutil.FakeSecuritySettingsRepository
import com.minhtu.firesocialmedia.testutil.FakeSettingsRepository
import com.minhtu.firesocialmedia.testutil.makeUserDTO
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class ChangePasswordViewModelTest {

    @BeforeTest
    fun setup() {
        Dispatchers.setMain(StandardTestDispatcher())
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun buildVm(
        settingsRepo: FakeSettingsRepository = FakeSettingsRepository(),
        securitySettingsRepo: FakeSecuritySettingsRepository = FakeSecuritySettingsRepository(),
        scheduler: kotlinx.coroutines.test.TestCoroutineScheduler
    ) = ChangePasswordViewModel(
        VerifyCurrentPasswordUseCase(settingsRepo),
        ValidateNewPasswordUseCase(),
        ChangePasswordUseCase(securitySettingsRepo),
        StandardTestDispatcher(scheduler)
    )

    @Test
    fun `updatePassword with empty fields sets DataEmptyError`() = runTest {
        val vm = buildVm(scheduler = testScheduler)
        vm.updatePassword(makeUserDTO())
        advanceUntilIdle()

        val state = vm.changePasswordState.value
        assertNotNull(state)
        assertFalse(state.isValid)
        assertEquals(ChangePasswordError.DataEmptyError, state.error)
    }

    @Test
    fun `updatePassword with mismatched passwords sets PasswordMismatchError`() = runTest {
        val vm = buildVm(scheduler = testScheduler)
        vm.updateCurrentPassword("Current@1")
        vm.updateNewPassword("NewPass@123")
        vm.updateConfirmPassword("Different@456")
        vm.updatePassword(makeUserDTO())
        advanceUntilIdle()

        val state = vm.changePasswordState.value
        assertNotNull(state)
        assertEquals(ChangePasswordError.PasswordMismatchError, state.error)
    }

    @Test
    fun `updatePassword with short new password sets PasswordShortError`() = runTest {
        val vm = buildVm(scheduler = testScheduler)
        vm.updateCurrentPassword("Current@1")
        vm.updateNewPassword("ab1")
        vm.updateConfirmPassword("ab1")
        vm.updatePassword(makeUserDTO())
        advanceUntilIdle()

        val state = vm.changePasswordState.value
        assertNotNull(state)
        assertEquals(ChangePasswordError.PasswordShortError, state.error)
    }

    @Test
    fun `updatePassword with wrong current password sets CurrentPasswordWrongError`() = runTest {
        val settingsRepo = FakeSettingsRepository().apply { reAuthResult = false }
        val vm = buildVm(settingsRepo, scheduler = testScheduler)
        vm.updateNewPassword("NewPass@123")
        vm.updateConfirmPassword("NewPass@123")
        vm.updateCurrentPassword("wrongpass")
        vm.updatePassword(makeUserDTO())
        advanceUntilIdle()

        val state = vm.changePasswordState.value
        assertNotNull(state)
        assertEquals(ChangePasswordError.CurrentPasswordWrongError, state.error)
    }

    @Test
    fun `updatePassword success sets isValid true`() = runTest {
        val settingsRepo = FakeSettingsRepository().apply { reAuthResult = true }
        val securitySettingsRepo = FakeSecuritySettingsRepository().apply {
            changePasswordResult = ChangePasswordState(true, null)
        }
        val vm = buildVm(settingsRepo, securitySettingsRepo, testScheduler)
        vm.updateCurrentPassword("Current@1")
        vm.updateNewPassword("NewPass@123")
        vm.updateConfirmPassword("NewPass@123")
        vm.updatePassword(makeUserDTO())
        advanceUntilIdle()

        val state = vm.changePasswordState.value
        assertNotNull(state)
        assertTrue(state.isValid)
    }

    @Test
    fun `resetChangePasswordState clears state`() = runTest {
        val vm = buildVm(scheduler = testScheduler)
        vm.updatePassword(makeUserDTO())
        advanceUntilIdle()
        assertNotNull(vm.changePasswordState.value)

        vm.resetChangePasswordState()

        assertNull(vm.changePasswordState.value)
    }

    @Test
    fun `retryWithReAuth with wrong password sets ReauthenticateFailedError`() = runTest {
        val settingsRepo = FakeSettingsRepository().apply { reAuthResult = false }
        val vm = buildVm(settingsRepo, scheduler = testScheduler)
        vm.updateCurrentPassword("wrongPass")
        vm.retryWithReAuth(makeUserDTO())
        advanceUntilIdle()

        val state = vm.changePasswordState.value
        assertNotNull(state)
        assertEquals(ChangePasswordError.ReauthenticateFailedError, state.error)
    }

    @Test
    fun `retryWithReAuth with correct password retries updatePassword`() = runTest {
        val settingsRepo = FakeSettingsRepository().apply { reAuthResult = true }
        val securitySettingsRepo = FakeSecuritySettingsRepository().apply {
            changePasswordResult = ChangePasswordState(true, null)
        }
        val vm = buildVm(settingsRepo, securitySettingsRepo, testScheduler)
        vm.updateCurrentPassword("Current@1")
        vm.updateNewPassword("NewPass@123")
        vm.updateConfirmPassword("NewPass@123")
        vm.retryWithReAuth(makeUserDTO())
        advanceUntilIdle()

        val state = vm.changePasswordState.value
        assertNotNull(state)
        assertTrue(state.isValid)
    }
}
