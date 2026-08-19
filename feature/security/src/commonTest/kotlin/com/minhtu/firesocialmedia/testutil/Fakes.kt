package com.minhtu.firesocialmedia.testutil

import com.minhtu.firesocialmedia.data.local.service.crypto.SecurityCryptoService
import com.minhtu.firesocialmedia.domain.entity.authentication.TwoFAResponse
import com.minhtu.firesocialmedia.domain.entity.settings.ChangePasswordState
import com.minhtu.firesocialmedia.domain.entity.settings.SessionItem
import com.minhtu.firesocialmedia.domain.repository.SecuritySettingsRepository
import com.minhtu.firesocialmedia.domain.repository.SettingsRepository
import com.minhtu.firesocialmedia.domain.repository.ShowImageRepository
import com.minhtu.firesocialmedia.domain.repository.TwoFactorAuthRepository
import com.minhtu.firesocialmedia.domain.repository.security.UserRepository
import com.minhtu.firesocialmedia.security.data.remote.dto.user.UserDTO
import com.minhtu.firesocialmedia.security.entity.user.UserInstance

// ─────────────────────────────────────────────────────────────
// Fake repositories used by ViewModel / UseCase tests.
// ─────────────────────────────────────────────────────────────

class FakeSecuritySettingsRepository : SecuritySettingsRepository {
    var changePasswordResult = ChangePasswordState(true, null)
    var copyInvoked: String? = null
    var fetchedSessions: List<SessionItem> = emptyList()
    var deleteSessionResult = true
    var logoutSessionResult = true
    var updateTimestampResult = true
    var clearLocalSessionInvoked = false

    var lastChangePasswordUser: UserInstance? = null
    var lastDeleteSessionArgs: Pair<String, String>? = null
    var lastLogoutSessionArgs: Pair<String, String>? = null
    var lastUpdateTimestampArgs: Triple<String, String, Long>? = null

    override suspend fun changePassword(user: UserInstance, newPassword: String): ChangePasswordState {
        lastChangePasswordUser = user
        return changePasswordResult
    }

    override suspend fun copy(data: String) {
        copyInvoked = data
    }

    override suspend fun fetchLoginHistoryList(userId: String): List<SessionItem> = fetchedSessions

    override suspend fun deleteLoginSession(userId: String, sessionId: String): Boolean {
        lastDeleteSessionArgs = userId to sessionId
        return deleteSessionResult
    }

    override suspend fun logoutSession(userId: String, sessionId: String): Boolean {
        lastLogoutSessionArgs = userId to sessionId
        return logoutSessionResult
    }

    override suspend fun updateUserTimestamp(userId: String, fieldPath: String, value: Long): Boolean {
        lastUpdateTimestampArgs = Triple(userId, fieldPath, value)
        return updateTimestampResult
    }

    override fun clearLocalSession() {
        clearLocalSessionInvoked = true
    }
}

class FakeSettingsRepository : SettingsRepository {
    var reAuthResult = true
    var get2FAVerifiedStatusResult = false
    var updateVerify2FAInvokedCount = 0
    var delete2FAStatusInLocalInvokedCount = 0
    var observeSessionStatusArgs: Triple<String, String, () -> Unit>? = null
    var stopObserveSessionStatusInvoked = false

    var lastReAuthArgs: Pair<String, String>? = null

    override suspend fun reAuthenticate(currentUserEmail: String, currentPassword: String): Boolean {
        lastReAuthArgs = currentUserEmail to currentPassword
        return reAuthResult
    }

    override suspend fun updateVerify2FASuccess() {
        updateVerify2FAInvokedCount++
    }

    override suspend fun get2FAVerifiedStatus(): Boolean = get2FAVerifiedStatusResult

    override suspend fun delete2FAStatusInLocal() {
        delete2FAStatusInLocalInvokedCount++
    }

    override fun observeSessionStatus(userId: String, sessionId: String, onLoggedOut: () -> Unit) {
        observeSessionStatusArgs = Triple(userId, sessionId, onLoggedOut)
    }

    override fun stopObserveSessionStatus() {
        stopObserveSessionStatusInvoked = true
    }
}

class FakeTwoFactorAuthRepository : TwoFactorAuthRepository {
    var generatedSecret = "ABCDEFGH1234"
    var enableOTPResult = TwoFAResponse(success = true, message = "ok")
    var verifyOTPResult = TwoFAResponse(success = true, message = "ok")
    var updateFlagResult = true
    var disable2FAResult = TwoFAResponse(success = true, message = "ok")
    var verifyBackupCodeResult = TwoFAResponse(success = true, message = "BACKUP123")

    var lastUpdateFlagArgs: Pair<String, Boolean>? = null
    var updateFlagInvokedCount = 0

    override suspend fun generateSecretFor2FA(): String = generatedSecret

    override suspend fun enableOTP(userId: String, secret: String, otpToVerify: String): TwoFAResponse = enableOTPResult

    override suspend fun verifyOTP(userId: String, otpToVerify: String): TwoFAResponse = verifyOTPResult

    override suspend fun updateTwoFAEnabledFlagForUser(userId: String, twoFAEnabled: Boolean): Boolean {
        lastUpdateFlagArgs = userId to twoFAEnabled
        updateFlagInvokedCount++
        return updateFlagResult
    }

    override suspend fun disable2FA(userId: String): TwoFAResponse = disable2FAResult

    override suspend fun verifyBackupCode(userId: String, backupCode: String): TwoFAResponse = verifyBackupCodeResult
}

class FakeShowImageRepository : ShowImageRepository {
    var downloadResult = true
    var lastArgs: Pair<String, String>? = null

    override suspend fun downloadImage(image: String, fileName: String): Boolean {
        lastArgs = image to fileName
        return downloadResult
    }
}

class FakeUserRepository(var currentUid: String? = "uid-test") : UserRepository {
    override suspend fun getCurrentUserUid(): String? = currentUid
}

class FakeSecurityCryptoService : SecurityCryptoService {
    var clearAccountInvokedCount = 0
    var saved2FAStatus: Boolean? = null
    var get2FAStatusResult = false
    var delete2FAStatusInvokedCount = 0

    override suspend fun clearAccount() {
        clearAccountInvokedCount++
    }

    override suspend fun save2FAStatus(status: Boolean) {
        saved2FAStatus = status
    }

    override suspend fun get2FAStatus(): Boolean = get2FAStatusResult

    override suspend fun delete2FAStatus() {
        delete2FAStatusInvokedCount++
    }
}

fun makeUserDTO(
    uid: String = "u1",
    email: String = "test@example.com",
    twoFAEnabled: Boolean = false
): UserDTO = UserDTO(uid = uid, name = "Test", email = email, twoFAEnabled = twoFAEnabled)

fun makeUserInstance(
    uid: String = "u1",
    email: String = "test@example.com",
    twoFAEnabled: Boolean = false
): UserInstance = UserInstance(uid = uid, name = "Test", email = email, twoFAEnabled = twoFAEnabled)
