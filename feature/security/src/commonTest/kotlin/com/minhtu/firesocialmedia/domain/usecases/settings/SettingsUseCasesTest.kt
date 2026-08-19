package com.minhtu.firesocialmedia.domain.usecases.settings

import com.minhtu.firesocialmedia.domain.entity.authentication.TwoFAResponse
import com.minhtu.firesocialmedia.domain.entity.settings.ChangePasswordState
import com.minhtu.firesocialmedia.domain.entity.settings.SessionItem
import com.minhtu.firesocialmedia.domain.error.changepassword.ChangePasswordError
import com.minhtu.firesocialmedia.testutil.FakeSecuritySettingsRepository
import com.minhtu.firesocialmedia.testutil.FakeSettingsRepository
import com.minhtu.firesocialmedia.testutil.FakeTwoFactorAuthRepository
import com.minhtu.firesocialmedia.testutil.makeUserInstance
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class BuildOtpAuthUrlUseCaseTest {
    @Test
    fun `invoke builds an otpauth url containing app, user, secret and issuer`() {
        val useCase = BuildOtpAuthUrlUseCase()
        val url = useCase("MyApp", "user@test.com", "SECRET123")
        assertEquals("otpauth://totp/MyApp:user@test.com?secret=SECRET123&issuer=MyApp", url)
    }

    @Test
    fun `invoke handles null secret`() {
        val useCase = BuildOtpAuthUrlUseCase()
        val url = useCase("MyApp", "user@test.com", null)
        assertTrue(url.contains("secret=null"))
    }
}

class ChangePasswordUseCaseTest {
    @Test
    fun `invoke delegates to repository and returns its result`() = runTest {
        val repo = FakeSecuritySettingsRepository().apply {
            changePasswordResult = ChangePasswordState(true, null)
        }
        val useCase = ChangePasswordUseCase(repo)
        val user = makeUserInstance()

        val result = useCase(user, "NewPass@123")

        assertTrue(result.isValid)
        assertEquals(user, repo.lastChangePasswordUser)
    }

    @Test
    fun `invoke surfaces failure state from repository`() = runTest {
        val repo = FakeSecuritySettingsRepository().apply {
            changePasswordResult = ChangePasswordState(false, ChangePasswordError.Unknown("network error"))
        }
        val useCase = ChangePasswordUseCase(repo)

        val result = useCase(makeUserInstance(), "NewPass@123")

        assertFalse(result.isValid)
        assertEquals(ChangePasswordError.Unknown("network error"), result.error)
    }
}

class CopyUseCaseTest {
    @Test
    fun `invoke forwards the data to the repository`() = runTest {
        val repo = FakeSecuritySettingsRepository()
        val useCase = CopyUseCase(repo)

        useCase("clip-me")

        assertEquals("clip-me", repo.copyInvoked)
    }
}

class DeleteLoginSessionUseCaseTest {
    @Test
    fun `invoke forwards args and returns repository result`() = runTest {
        val repo = FakeSecuritySettingsRepository().apply { deleteSessionResult = true }
        val useCase = DeleteLoginSessionUseCase(repo)

        val result = useCase("u1", "s1")

        assertTrue(result)
        assertEquals("u1" to "s1", repo.lastDeleteSessionArgs)
    }
}

class Disable2FAUseCaseTest {
    @Test
    fun `invoke on success flips twoFAEnabled flag off and clears local status`() = runTest {
        val twoFARepo = FakeTwoFactorAuthRepository().apply {
            disable2FAResult = TwoFAResponse(success = true, message = "ok")
        }
        val settingsRepo = FakeSettingsRepository()
        val useCase = Disable2FAUseCase(twoFARepo, settingsRepo)
        val user = makeUserInstance(twoFAEnabled = true)

        val result = useCase(user)

        assertTrue(result.success)
        assertEquals("u1" to false, twoFARepo.lastUpdateFlagArgs)
        assertEquals(1, settingsRepo.delete2FAStatusInLocalInvokedCount)
    }

    @Test
    fun `invoke on failure does not touch flag or local status`() = runTest {
        val twoFARepo = FakeTwoFactorAuthRepository().apply {
            disable2FAResult = TwoFAResponse(success = false, message = "server error")
        }
        val settingsRepo = FakeSettingsRepository()
        val useCase = Disable2FAUseCase(twoFARepo, settingsRepo)

        val result = useCase(makeUserInstance())

        assertFalse(result.success)
        assertEquals(0, twoFARepo.updateFlagInvokedCount)
        assertEquals(0, settingsRepo.delete2FAStatusInLocalInvokedCount)
    }
}

class Enable2FAUseCaseTest {
    @Test
    fun `invoke on correct otp flips twoFAEnabled flag on`() = runTest {
        val twoFARepo = FakeTwoFactorAuthRepository().apply {
            enableOTPResult = TwoFAResponse(success = true, message = "ok")
        }
        val useCase = Enable2FAUseCase(twoFARepo)
        val user = makeUserInstance(twoFAEnabled = false)

        val result = useCase(user, "SECRET", "123456")

        assertTrue(result.success)
        assertEquals("u1" to true, twoFARepo.lastUpdateFlagArgs)
    }

    @Test
    fun `invoke on wrong otp leaves flag untouched`() = runTest {
        val twoFARepo = FakeTwoFactorAuthRepository().apply {
            enableOTPResult = TwoFAResponse(success = false, message = "invalid otp")
        }
        val useCase = Enable2FAUseCase(twoFARepo)

        val result = useCase(makeUserInstance(), "SECRET", "000000")

        assertFalse(result.success)
        assertEquals(0, twoFARepo.updateFlagInvokedCount)
    }
}

class FetchLoginHistoryListUseCaseTest {
    @Test
    fun `invoke returns sessions from the repository`() = runTest {
        val sessions = listOf(SessionItem(sessionId = "s1"), SessionItem(sessionId = "s2"))
        val repo = FakeSecuritySettingsRepository().apply { fetchedSessions = sessions }
        val useCase = FetchLoginHistoryListUseCase(repo)

        assertEquals(sessions, useCase("u1"))
    }
}

class GenerateSecretFor2FAUseCaseTest {
    @Test
    fun `invoke returns the generated secret`() = runTest {
        val repo = FakeTwoFactorAuthRepository().apply { generatedSecret = "SECRET-XYZ" }
        val useCase = GenerateSecretFor2FAUseCase(repo)

        assertEquals("SECRET-XYZ", useCase())
    }
}

class Get2FAVerifiedStatusUseCaseTest {
    @Test
    fun `invoke returns the verified status from the repository`() = runTest {
        val repo = FakeSettingsRepository().apply { get2FAVerifiedStatusResult = true }
        val useCase = Get2FAVerifiedStatusUseCase(repo)

        assertTrue(useCase())
    }
}

class LogoutSessionUseCaseTest {
    @Test
    fun `invoke forwards args and returns repository result`() = runTest {
        val repo = FakeSecuritySettingsRepository().apply { logoutSessionResult = false }
        val useCase = LogoutSessionUseCase(repo)

        val result = useCase("u1", "s1")

        assertFalse(result)
        assertEquals("u1" to "s1", repo.lastLogoutSessionArgs)
    }
}

class ObserveSessionStatusUseCaseTest {
    @Test
    fun `invoke registers the callback with the repository and it can be triggered`() = runTest {
        val repo = FakeSettingsRepository()
        val useCase = ObserveSessionStatusUseCase(repo)
        var loggedOut = false

        useCase("u1", "s1") { loggedOut = true }

        val args = repo.observeSessionStatusArgs
        assertEquals("u1", args?.first)
        assertEquals("s1", args?.second)
        args?.third?.invoke()
        assertTrue(loggedOut)
    }

    @Test
    fun `stop use case forwards to repository`() = runTest {
        val repo = FakeSettingsRepository()
        val useCase = StopObserveSessionStatusUseCase(repo)

        useCase()

        assertTrue(repo.stopObserveSessionStatusInvoked)
    }
}

class UpdateUserTimestampUseCaseTest {
    @Test
    fun `invoke forwards args and returns repository result`() = runTest {
        val repo = FakeSecuritySettingsRepository().apply { updateTimestampResult = true }
        val useCase = UpdateUserTimestampUseCase(repo)

        val result = useCase.invoke("u1", "field", 123456L)

        assertTrue(result)
        assertEquals(Triple("u1", "field", 123456L), repo.lastUpdateTimestampArgs)
    }
}

class UpdateVerify2FASuccessUseCaseTest {
    @Test
    fun `invoke delegates to the repository`() = runTest {
        val repo = FakeSettingsRepository()
        val useCase = UpdateVerify2FASuccessUseCase(repo)

        useCase()

        assertEquals(1, repo.updateVerify2FAInvokedCount)
    }
}

class ValidateNewPasswordUseCaseTest {
    private val useCase = ValidateNewPasswordUseCase()

    @Test
    fun `empty new password sets DataEmptyError`() {
        val result = useCase("", "")
        assertFalse(result.isValid)
        assertEquals(ChangePasswordError.DataEmptyError, result.error)
    }

    @Test
    fun `empty new password takes priority over mismatch`() {
        val result = useCase("", "something")
        assertEquals(ChangePasswordError.DataEmptyError, result.error)
    }

    @Test
    fun `mismatched passwords set PasswordMismatchError`() {
        val result = useCase("NewPass@123", "Different@456")
        assertFalse(result.isValid)
        assertEquals(ChangePasswordError.PasswordMismatchError, result.error)
    }

    @Test
    fun `short password sets PasswordShortError`() {
        val result = useCase("abc12", "abc12")
        assertFalse(result.isValid)
        assertEquals(ChangePasswordError.PasswordShortError, result.error)
    }

    @Test
    fun `valid matching password of sufficient length is valid`() {
        val result = useCase("NewPass@123", "NewPass@123")
        assertTrue(result.isValid)
        assertEquals(null, result.error)
    }

    @Test
    fun `password exactly at minimum length boundary is valid`() {
        val result = useCase("abcdef", "abcdef")
        assertTrue(result.isValid)
    }
}

class Verify2FAUseCaseTest {
    @Test
    fun `invoke on correct otp updates local verified status`() = runTest {
        val twoFARepo = FakeTwoFactorAuthRepository().apply {
            verifyOTPResult = TwoFAResponse(success = true, message = "ok")
        }
        val settingsRepo = FakeSettingsRepository()
        val useCase = Verify2FAUseCase(twoFARepo, settingsRepo)

        val result = useCase(makeUserInstance(), "123456")

        assertTrue(result.success)
        assertEquals(1, settingsRepo.updateVerify2FAInvokedCount)
    }

    @Test
    fun `invoke on wrong otp does not update local verified status`() = runTest {
        val twoFARepo = FakeTwoFactorAuthRepository().apply {
            verifyOTPResult = TwoFAResponse(success = false, message = "invalid otp")
        }
        val settingsRepo = FakeSettingsRepository()
        val useCase = Verify2FAUseCase(twoFARepo, settingsRepo)

        val result = useCase(makeUserInstance(), "000000")

        assertFalse(result.success)
        assertEquals(0, settingsRepo.updateVerify2FAInvokedCount)
    }
}

class VerifyBackupCodeUseCaseTest {
    @Test
    fun `invoke on correct backup code updates local verified status`() = runTest {
        val twoFARepo = FakeTwoFactorAuthRepository().apply {
            verifyBackupCodeResult = TwoFAResponse(success = true, message = "BACKUP123")
        }
        val settingsRepo = FakeSettingsRepository()
        val useCase = VerifyBackupCodeUseCase(twoFARepo, settingsRepo)

        val result = useCase("u1", "BACKUP123")

        assertTrue(result.success)
        assertEquals(1, settingsRepo.updateVerify2FAInvokedCount)
    }

    @Test
    fun `invoke on wrong backup code does not update local verified status`() = runTest {
        val twoFARepo = FakeTwoFactorAuthRepository().apply {
            verifyBackupCodeResult = TwoFAResponse(success = false, message = "invalid code")
        }
        val settingsRepo = FakeSettingsRepository()
        val useCase = VerifyBackupCodeUseCase(twoFARepo, settingsRepo)

        val result = useCase("u1", "WRONG")

        assertFalse(result.success)
        assertEquals(0, settingsRepo.updateVerify2FAInvokedCount)
    }
}

class VerifyCurrentPasswordUseCaseTest {
    @Test
    fun `invoke returns true for correct credentials`() = runTest {
        val repo = FakeSettingsRepository().apply { reAuthResult = true }
        val useCase = VerifyCurrentPasswordUseCase(repo)

        assertTrue(useCase("test@example.com", "correct-pass"))
        assertEquals("test@example.com" to "correct-pass", repo.lastReAuthArgs)
    }

    @Test
    fun `invoke returns false for wrong password`() = runTest {
        val repo = FakeSettingsRepository().apply { reAuthResult = false }
        val useCase = VerifyCurrentPasswordUseCase(repo)

        assertFalse(useCase("test@example.com", "wrong-pass"))
    }
}
