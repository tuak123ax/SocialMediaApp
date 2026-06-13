package com.minhtu.firesocialmedia.feature.security

import com.minhtu.firesocialmedia.core.domain.entity.authentication.TwoFAResponse
import com.minhtu.firesocialmedia.core.domain.entity.crypto.Credentials
import com.minhtu.firesocialmedia.core.domain.entity.forgotpassword.EmailExistResult
import com.minhtu.firesocialmedia.core.domain.entity.settings.ChangePasswordState
import com.minhtu.firesocialmedia.core.domain.entity.settings.PollObject
import com.minhtu.firesocialmedia.core.domain.entity.settings.SessionItem
import com.minhtu.firesocialmedia.core.domain.entity.signin.SignInState
import com.minhtu.firesocialmedia.core.domain.entity.user.UserInstance
import com.minhtu.firesocialmedia.core.domain.error.changepassword.ChangePasswordError
import com.minhtu.firesocialmedia.core.domain.error.signin.SignInError
import com.minhtu.firesocialmedia.core.domain.repository.AuthenticationRepository
import com.minhtu.firesocialmedia.core.domain.repository.SettingsRepository
import com.minhtu.firesocialmedia.core.domain.repository.UserRepository
import com.minhtu.firesocialmedia.core.domain.usecases.common.GetCurrentUserUidUseCase
import com.minhtu.firesocialmedia.core.domain.usecases.home.ClearAccountUseCase
import com.minhtu.firesocialmedia.core.domain.usecases.settings.BuildOtpAuthUrlUseCase
import com.minhtu.firesocialmedia.core.domain.usecases.settings.ChangePasswordUseCase
import com.minhtu.firesocialmedia.core.domain.usecases.settings.CopyUseCase
import com.minhtu.firesocialmedia.core.domain.usecases.settings.DeleteLoginSessionUseCase
import com.minhtu.firesocialmedia.core.domain.usecases.settings.Disable2FAUseCase
import com.minhtu.firesocialmedia.core.domain.usecases.settings.Enable2FAUseCase
import com.minhtu.firesocialmedia.core.domain.usecases.settings.FetchLoginHistoryListUseCase
import com.minhtu.firesocialmedia.core.domain.usecases.settings.GenerateSecretFor2FAUseCase
import com.minhtu.firesocialmedia.core.domain.usecases.settings.LogoutSessionUseCase
import com.minhtu.firesocialmedia.core.domain.usecases.settings.UpdateUserTimestampUseCase
import com.minhtu.firesocialmedia.core.domain.usecases.settings.UpdateVerify2FASuccessUseCase
import com.minhtu.firesocialmedia.core.domain.usecases.settings.ValidateNewPasswordUseCase
import com.minhtu.firesocialmedia.core.domain.usecases.settings.Verify2FAUseCase
import com.minhtu.firesocialmedia.core.domain.usecases.settings.VerifyBackupCodeUseCase
import com.minhtu.firesocialmedia.core.domain.usecases.settings.VerifyCurrentPasswordUseCase
import com.minhtu.firesocialmedia.feature.security.presentation.changepassword.ChangePasswordViewModel
import com.minhtu.firesocialmedia.feature.security.presentation.loginhistory.AcknowledgeStatus
import com.minhtu.firesocialmedia.feature.security.presentation.loginhistory.LoginHistoryUiState
import com.minhtu.firesocialmedia.feature.security.presentation.loginhistory.LoginHistoryViewModel
import com.minhtu.firesocialmedia.feature.security.presentation.loginhistory.LogoutSessionStatus
import com.minhtu.firesocialmedia.feature.security.presentation.settings.SecuritySettingsViewModel
import com.minhtu.firesocialmedia.feature.security.presentation.twofa.BackUpCodeViewModel
import com.minhtu.firesocialmedia.feature.security.presentation.twofa.TwoFAViewModel
import com.minhtu.firesocialmedia.feature.security.presentation.twofa.TwoFactorEnabledViewModel
import com.minhtu.firesocialmedia.feature.security.presentation.twofa.VerifyOTPViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

// ─────────────────────────────────────────────────────────────
// Fake repositories
// ─────────────────────────────────────────────────────────────

private class FakeSettingsRepository : SettingsRepository {
    var disable2FAResult = TwoFAResponse(success = true, message = "ok")
    var changePasswordResult = ChangePasswordState(true, null)
    var reAuthResult = true
    var generatedSecret = "ABCDEFGH1234"
    var fetchedSessions: List<SessionItem> = emptyList()
    var deleteSessionResult = true
    var logoutSessionResult = true
    var updateTimestampResult = true
    var verifyBackupCodeResult = TwoFAResponse(success = true, message = "BACKUP123")
    var enableOTPResult = TwoFAResponse(success = true, message = "BACKUP")
    var verifyOTPResult = TwoFAResponse(success = true, message = "ok")
    var copyInvoked: String? = null
    var updateVerify2FAInvokedCount = 0

    override suspend fun disable2FA(userId: String): TwoFAResponse = disable2FAResult
    override suspend fun changePassword(user: UserInstance, newPassword: String): ChangePasswordState = changePasswordResult
    override suspend fun reAuthenticate(currentUserEmail: String, currentPassword: String): Boolean = reAuthResult
    override suspend fun generateSecretFor2FA(): String = generatedSecret
    override suspend fun copy(data: String) { copyInvoked = data }
    override suspend fun enableOTP(userId: String, secret: String, otpToVerify: String): TwoFAResponse = enableOTPResult
    override suspend fun verifyOTP(userId: String, otpToVerify: String): TwoFAResponse = verifyOTPResult
    override suspend fun updateTwoFAEnabledFlagForUser(userId: String, twoFAEnabled: Boolean): Boolean = true
    override suspend fun verifyBackupCode(userId: String, backupCode: String): TwoFAResponse = verifyBackupCodeResult
    override suspend fun updateVerify2FASuccess() {
        updateVerify2FAInvokedCount++
    }
    override suspend fun get2FAVerifiedStatus(): Boolean = false
    override suspend fun delete2FAStatusInLocal() {}
    override suspend fun fetchLoginHistoryList(userId: String): List<SessionItem> = fetchedSessions
    override suspend fun deleteLoginSession(userId: String, sessionId: String): Boolean = deleteSessionResult
    override suspend fun logoutSession(userId: String, sessionId: String): Boolean = logoutSessionResult
    override fun observeSessionStatus(userId: String, sessionId: String, onLoggedOut: () -> Unit) {}
    override fun stopObserveSessionStatus() {}
    override suspend fun updateUserTimestamp(userId: String, fieldPath: String, value: Long): Boolean = updateTimestampResult
    override suspend fun updateUserStringField(userId: String, fieldPath: String, value: String): Boolean = true
    override suspend fun updateUserAvatar(userId: String, imageUri: String): Boolean = true
    override suspend fun updateUserBackground(userId: String, imageUri: String): Boolean = true
    override suspend fun createPoll(poll: PollObject, newsId: String, groupId: String): Boolean = true
}

private class FakeAuthRepository : AuthenticationRepository {
    var cleared = false
    override suspend fun signInWithEmailAndPassword(email: String, password: String): SignInError? = null
    override suspend fun saveAccountToLocalStorage(email: String, password: String) {}
    override suspend fun checkUserExists(email: String): SignInState = SignInState(true, null)
    override suspend fun checkLocalAccount(): Credentials? = null
    override suspend fun handleSignInGoogleResult(credential: Any): String? = null
    override suspend fun signUpWithEmailAndPassword(email: String, password: String): Result<Unit> = Result.success(Unit)
    override suspend fun fetchSignInMethodsForEmail(email: String): EmailExistResult = EmailExistResult(false, "")
    override suspend fun sendPasswordResetEmail(email: String): Boolean = true
    override suspend fun clearAccount() { cleared = true }
    override suspend fun saveSignUpInformation(userInstance: UserInstance): Boolean = true
}

private class FakeUserRepository(var currentUid: String? = "uid-test") : UserRepository {
    override suspend fun getCurrentUserUid(): String? = currentUid
    override suspend fun getUser(userId: String, isCurrentUser: Boolean): UserInstance? = null
    override suspend fun updateFCMTokenForCurrentUser(user: UserInstance) {}
    override suspend fun searchUserByName(name: String): List<UserInstance>? = null
}

private fun makeUser(
    uid: String = "u1",
    email: String = "test@example.com",
    twoFAEnabled: Boolean = false
): UserInstance = UserInstance(uid = uid, name = "Test", email = email).also {
    it.twoFAEnabled = twoFAEnabled
}

// ─────────────────────────────────────────────────────────────
// SecuritySettingsViewModel Tests
// ─────────────────────────────────────────────────────────────

@OptIn(ExperimentalCoroutinesApi::class)
class SecuritySettingsViewModelTest {

    @Test
    fun `disable2FA emits success response`() = runTest {
        val repo = FakeSettingsRepository()
        val vm = SecuritySettingsViewModel(
            Disable2FAUseCase(repo),
            StandardTestDispatcher(testScheduler)
        )
        vm.disable2FA(makeUser())
        advanceUntilIdle()

        val result = vm.disable2FAStatus.value
        assertNotNull(result)
        assertTrue(result.success)
    }

    @Test
    fun `disable2FA emits failure when repo fails`() = runTest {
        val repo = FakeSettingsRepository().apply {
            disable2FAResult = TwoFAResponse(success = false, message = "error")
        }
        val vm = SecuritySettingsViewModel(
            Disable2FAUseCase(repo),
            StandardTestDispatcher(testScheduler)
        )
        vm.disable2FA(makeUser())
        advanceUntilIdle()

        val result = vm.disable2FAStatus.value
        assertNotNull(result)
        assertTrue(!result.success)
    }

    @Test
    fun `resetDisable2FAStatus clears the state`() = runTest {
        val repo = FakeSettingsRepository()
        val vm = SecuritySettingsViewModel(
            Disable2FAUseCase(repo),
            StandardTestDispatcher(testScheduler)
        )
        vm.disable2FA(makeUser())
        advanceUntilIdle()
        assertNotNull(vm.disable2FAStatus.value)

        vm.resetDisable2FAStatus()

        assertNull(vm.disable2FAStatus.value)
    }
}

// ─────────────────────────────────────────────────────────────
// ChangePasswordViewModel Tests
// ─────────────────────────────────────────────────────────────

@OptIn(ExperimentalCoroutinesApi::class)
class ChangePasswordViewModelTest {

    private fun buildVm(
        repo: FakeSettingsRepository = FakeSettingsRepository(),
        scheduler: kotlinx.coroutines.test.TestCoroutineScheduler
    ): Pair<ChangePasswordViewModel, FakeSettingsRepository> {
        val vm = ChangePasswordViewModel(
            VerifyCurrentPasswordUseCase(repo),
            ValidateNewPasswordUseCase(),
            ChangePasswordUseCase(repo),
            StandardTestDispatcher(scheduler)
        )
        return vm to repo
    }

    @Test
    fun `updatePassword with empty fields sets DataEmptyError`() = runTest {
        val (vm, _) = buildVm(scheduler = testScheduler)
        vm.updatePassword(makeUser())
        advanceUntilIdle()

        val state = vm.changePasswordState.value
        assertNotNull(state)
        assertTrue(!state.isValid)
        assertEquals(ChangePasswordError.DataEmptyError, state.error)
    }

    @Test
    fun `updatePassword with wrong current password sets CurrentPasswordWrongError`() = runTest {
        val repo = FakeSettingsRepository().apply { reAuthResult = false }
        val (vm, _) = buildVm(repo, testScheduler)
        vm.updateNewPassword("NewPass@123")
        vm.updateConfirmPassword("NewPass@123")
        vm.updateCurrentPassword("wrongpass")
        vm.updatePassword(makeUser())
        advanceUntilIdle()

        val state = vm.changePasswordState.value
        assertNotNull(state)
        assertEquals(ChangePasswordError.CurrentPasswordWrongError, state.error)
    }

    @Test
    fun `updatePassword success sets isValid true`() = runTest {
        val repo = FakeSettingsRepository().apply {
            reAuthResult = true
            changePasswordResult = ChangePasswordState(true, null)
        }
        val (vm, _) = buildVm(repo, testScheduler)
        vm.updateCurrentPassword("Current@1")
        vm.updateNewPassword("NewPass@123")
        vm.updateConfirmPassword("NewPass@123")
        vm.updatePassword(makeUser())
        advanceUntilIdle()

        val state = vm.changePasswordState.value
        assertNotNull(state)
        assertTrue(state.isValid)
    }

    @Test
    fun `updatePassword with mismatched passwords sets PasswordMismatchError`() = runTest {
        val (vm, _) = buildVm(scheduler = testScheduler)
        vm.updateCurrentPassword("Current@1")
        vm.updateNewPassword("NewPass@123")
        vm.updateConfirmPassword("Different@456")
        vm.updatePassword(makeUser())
        advanceUntilIdle()

        val state = vm.changePasswordState.value
        assertNotNull(state)
        assertEquals(ChangePasswordError.PasswordMismatchError, state.error)
    }

    @Test
    fun `resetChangePasswordState clears state`() = runTest {
        val (vm, _) = buildVm(scheduler = testScheduler)
        vm.updatePassword(makeUser())
        advanceUntilIdle()
        assertNotNull(vm.changePasswordState.value)

        vm.resetChangePasswordState()

        assertNull(vm.changePasswordState.value)
    }

    @Test
    fun `retryWithReAuth with wrong password sets ReauthenticateFailedError`() = runTest {
        val mainDispatcher = StandardTestDispatcher(testScheduler)
        kotlinx.coroutines.Dispatchers.setMain(mainDispatcher)
        try {
            val repo = FakeSettingsRepository().apply { reAuthResult = false }
            val (vm, _) = buildVm(repo, testScheduler)
            vm.updateCurrentPassword("wrongPass")
            vm.retryWithReAuth(makeUser())
            advanceUntilIdle()

            val state = vm.changePasswordState.value
            assertNotNull(state)
            assertEquals(ChangePasswordError.ReauthenticateFailedError, state.error)
        } finally {
            kotlinx.coroutines.Dispatchers.resetMain()
        }
    }
}

// ─────────────────────────────────────────────────────────────
// LoginHistoryViewModel Tests
// ─────────────────────────────────────────────────────────────

@OptIn(ExperimentalCoroutinesApi::class)
class LoginHistoryViewModelTest {

    private fun buildVm(
        repo: FakeSettingsRepository = FakeSettingsRepository(),
        scheduler: kotlinx.coroutines.test.TestCoroutineScheduler
    ): Pair<LoginHistoryViewModel, FakeSettingsRepository> {
        val vm = LoginHistoryViewModel(
            FetchLoginHistoryListUseCase(repo),
            UpdateUserTimestampUseCase(repo),
            DeleteLoginSessionUseCase(repo),
            LogoutSessionUseCase(repo),
            VerifyCurrentPasswordUseCase(repo),
            StandardTestDispatcher(scheduler)
        )
        return vm to repo
    }

    @Test
    fun `fetchLoginHistoryList with empty list emits Empty state`() = runTest {
        val repo = FakeSettingsRepository().apply { fetchedSessions = emptyList() }
        val (vm, _) = buildVm(repo, testScheduler)
        vm.fetchLoginHistoryList("u1")
        advanceUntilIdle()

        assertEquals(LoginHistoryUiState.Empty, vm.loginHistoryUiState.value)
    }

    @Test
    fun `fetchLoginHistoryList with sessions emits Success state`() = runTest {
        val sessions = listOf(SessionItem(sessionId = "s1", deviceName = "iPhone"))
        val repo = FakeSettingsRepository().apply { fetchedSessions = sessions }
        val (vm, _) = buildVm(repo, testScheduler)
        vm.fetchLoginHistoryList("u1")
        advanceUntilIdle()

        val state = vm.loginHistoryUiState.value
        assertTrue(state is LoginHistoryUiState.Success)
        assertEquals(1, state.data.size)
    }

    @Test
    fun `deleteLoginSession with wrong password sets WRONG_PASSWORD status`() = runTest {
        val repo = FakeSettingsRepository().apply { reAuthResult = false }
        val (vm, _) = buildVm(repo, testScheduler)
        vm.deleteLoginSession("u1", "test@example.com", "wrongPass", "s1")
        advanceUntilIdle()

        assertEquals(LogoutSessionStatus.WRONG_PASSWORD, vm.logoutSessionStatus.value)
    }

    @Test
    fun `deleteLoginSession success triggers fetchLoginHistoryList`() = runTest {
        val sessions = listOf(SessionItem(sessionId = "s2", deviceName = "Android"))
        val repo = FakeSettingsRepository().apply {
            reAuthResult = true
            deleteSessionResult = true
            fetchedSessions = sessions
        }
        val (vm, _) = buildVm(repo, testScheduler)
        vm.deleteLoginSession("u1", "test@example.com", "correct", "s2")
        advanceUntilIdle()

        assertTrue(vm.loginHistoryUiState.value is LoginHistoryUiState.Success)
    }

    @Test
    fun `logoutSession with wrong password sets WRONG_PASSWORD status`() = runTest {
        val repo = FakeSettingsRepository().apply { reAuthResult = false }
        val (vm, _) = buildVm(repo, testScheduler)
        vm.logoutSession("u1", "test@example.com", "wrongPass", "s1")
        advanceUntilIdle()

        assertEquals(LogoutSessionStatus.WRONG_PASSWORD, vm.logoutSessionStatus.value)
    }

    @Test
    fun `acknowledgeLoginHistory sets SUCCESS on success`() = runTest {
        val repo = FakeSettingsRepository().apply { updateTimestampResult = true }
        val (vm, _) = buildVm(repo, testScheduler)
        val user = makeUser()
        vm.acknowledgeLoginHistory(user)
        advanceUntilIdle()

        assertEquals(AcknowledgeStatus.SUCCESS, vm.acknowledgeStatus.value)
        assertTrue(user.lastTimeAcknowledgedLoginHistory > 0)
    }

    @Test
    fun `acknowledgeLoginHistory sets ERROR on failure`() = runTest {
        val repo = FakeSettingsRepository().apply { updateTimestampResult = false }
        val (vm, _) = buildVm(repo, testScheduler)
        vm.acknowledgeLoginHistory(makeUser())
        advanceUntilIdle()

        assertEquals(AcknowledgeStatus.ERROR, vm.acknowledgeStatus.value)
    }

    @Test
    fun `resetAcknowledgeStatus restores IDLE`() = runTest {
        val repo = FakeSettingsRepository().apply { updateTimestampResult = true }
        val (vm, _) = buildVm(repo, testScheduler)
        vm.acknowledgeLoginHistory(makeUser())
        advanceUntilIdle()
        assertEquals(AcknowledgeStatus.SUCCESS, vm.acknowledgeStatus.value)

        vm.resetAcknowledgeStatus()
        assertEquals(AcknowledgeStatus.IDLE, vm.acknowledgeStatus.value)
    }
}

// ─────────────────────────────────────────────────────────────
// TwoFAViewModel Tests
// ─────────────────────────────────────────────────────────────

@OptIn(ExperimentalCoroutinesApi::class)
class TwoFAViewModelTest {

    private fun buildVm(
        repo: FakeSettingsRepository = FakeSettingsRepository(),
        scheduler: kotlinx.coroutines.test.TestCoroutineScheduler
    ) = TwoFAViewModel(
        GenerateSecretFor2FAUseCase(repo),
        BuildOtpAuthUrlUseCase(),
        CopyUseCase(repo),
        StandardTestDispatcher(scheduler)
    )

    @Test
    fun `generateSecretFor2FA populates secretFor2FA`() = runTest {
        val vm = buildVm(scheduler = testScheduler)
        vm.generateSecretFor2FA()
        advanceUntilIdle()

        assertNotNull(vm.secretFor2FA.value)
        assertEquals("ABCDEFGH1234", vm.secretFor2FA.value)
    }

    @Test
    fun `buildOtpAuthUrl returns non-empty string`() = runTest {
        val vm = buildVm(scheduler = testScheduler)
        val url = vm.buildOtpAuthUrl("MyApp", "user@test.com", "SECRET")
        assertTrue(url.isNotEmpty())
        assertTrue(url.startsWith("otpauth://totp/"))
    }

    @Test
    fun `copySecret invokes copy use case`() = runTest {
        val repo = FakeSettingsRepository()
        val vm = buildVm(repo, testScheduler)
        vm.copySecret("MY_SECRET_KEY")
        advanceUntilIdle()

        assertEquals("MY_SECRET_KEY", repo.copyInvoked)
    }
}

// ─────────────────────────────────────────────────────────────
// VerifyOTPViewModel Tests
// ─────────────────────────────────────────────────────────────

@OptIn(ExperimentalCoroutinesApi::class)
class VerifyOTPViewModelTest {

    private fun buildVm(
        settingsRepo: FakeSettingsRepository = FakeSettingsRepository(),
        authRepo: FakeAuthRepository = FakeAuthRepository(),
        scheduler: kotlinx.coroutines.test.TestCoroutineScheduler
    ) = VerifyOTPViewModel(
        Enable2FAUseCase(settingsRepo),
        Verify2FAUseCase(settingsRepo),
        ClearAccountUseCase(authRepo),
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
    fun `enableOTP sets success result`() = runTest {
        val vm = buildVm(scheduler = testScheduler)
        vm.enableOTP(makeUser(), "SECRET", "123456")
        advanceUntilIdle()

        val result = vm.verifyOTPResult.value
        assertNotNull(result)
        assertTrue(result.success)
    }

    @Test
    fun `verifyOTP sets success result`() = runTest {
        val vm = buildVm(scheduler = testScheduler)
        vm.verifyOTP(makeUser(), "123456")
        advanceUntilIdle()

        val result = vm.verifyOTPResult.value
        assertNotNull(result)
        assertTrue(result.success)
    }

    @Test
    fun `resetVerifyOTPResult clears result`() = runTest {
        val vm = buildVm(scheduler = testScheduler)
        vm.verifyOTP(makeUser(), "123456")
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
    fun `clearAccountInLocalData invokes clearAccount`() = runTest {
        val authRepo = FakeAuthRepository()
        val vm = buildVm(authRepo = authRepo, scheduler = testScheduler)
        vm.clearAccountInLocalData()
        advanceUntilIdle()
        assertTrue(authRepo.cleared)
    }
}

// ─────────────────────────────────────────────────────────────
// BackUpCodeViewModel Tests
// ─────────────────────────────────────────────────────────────

@OptIn(ExperimentalCoroutinesApi::class)
class BackUpCodeViewModelTest {

    private fun buildVm(
        settingsRepo: FakeSettingsRepository = FakeSettingsRepository(),
        userRepo: FakeUserRepository = FakeUserRepository(),
        scheduler: kotlinx.coroutines.test.TestCoroutineScheduler
    ) = BackUpCodeViewModel(
        GetCurrentUserUidUseCase(userRepo),
        VerifyBackupCodeUseCase(settingsRepo),
        StandardTestDispatcher(scheduler)
    )

    @Test
    fun `updateBackupCode updates state`() = runTest {
        val vm = buildVm(scheduler = testScheduler)
        vm.updateBackupCode("ABCDE-12345")
        assertEquals("ABCDE-12345", vm.backupCode.value)
    }

    @Test
    fun `verifyBackupCode with valid userId sets success result`() = runTest {
        val vm = buildVm(scheduler = testScheduler)
        vm.verifyBackupCode("ABCDE12345")
        advanceUntilIdle()

        val result = vm.verifyBackupCodeStatus.value
        assertNotNull(result)
        assertTrue(result.success)
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

// ─────────────────────────────────────────────────────────────
// TwoFactorEnabledViewModel Tests
// ─────────────────────────────────────────────────────────────

@OptIn(ExperimentalCoroutinesApi::class)
class TwoFactorEnabledViewModelTest {

    private fun buildVm(
        repo: FakeSettingsRepository = FakeSettingsRepository(),
        scheduler: kotlinx.coroutines.test.TestCoroutineScheduler
    ) = TwoFactorEnabledViewModel(
        CopyUseCase(repo),
        UpdateVerify2FASuccessUseCase(repo),
        StandardTestDispatcher(scheduler)
    )

    @Test
    fun `copyToClipboard invokes copy use case`() = runTest {
        val repo = FakeSettingsRepository()
        val vm = buildVm(repo, testScheduler)
        vm.copyToClipboard("MY-BACKUP-CODE")
        advanceUntilIdle()

        assertEquals("MY-BACKUP-CODE", repo.copyInvoked)
    }

    @Test
    fun `updateVerify2FASuccess completes without error`() = runTest {
        val repo = FakeSettingsRepository()
        val vm = buildVm(repo = repo, scheduler = testScheduler)
        vm.updateVerify2FASuccess()
        advanceUntilIdle()
        assertEquals(1, repo.updateVerify2FAInvokedCount)
    }
}


