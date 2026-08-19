package com.minhtu.firesocialmedia.presentation.navigation

import com.minhtu.firesocialmedia.appinit.data.remote.dto.user.UserDTO
import com.minhtu.firesocialmedia.constants.AuthRouteNames
import com.minhtu.firesocialmedia.domain.entity.crypto.Credentials
import com.minhtu.firesocialmedia.domain.entity.forgotpassword.EmailExistResult
import com.minhtu.firesocialmedia.domain.entity.signin.SignInState
import com.minhtu.firesocialmedia.domain.entity.user.auth.UserInstance
import com.minhtu.firesocialmedia.domain.error.signin.SignInError
import com.minhtu.firesocialmedia.domain.repository.AuthenticationRepository
import com.minhtu.firesocialmedia.domain.repository.SettingsRepository
import com.minhtu.firesocialmedia.domain.repository.appinit.UserRepository
import com.minhtu.firesocialmedia.domain.usecases.common.appinit.GetCurrentUserUidUseCase
import com.minhtu.firesocialmedia.domain.usecases.common.appinit.GetUserUseCase
import com.minhtu.firesocialmedia.domain.usecases.settings.Get2FAVerifiedStatusUseCase
import com.minhtu.firesocialmedia.domain.usecases.signin.CheckLocalAccountUseCase
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlin.test.Test
import kotlin.test.assertEquals

private class FakeAuthenticationRepository(
    var localCredentials: Credentials? = null
) : AuthenticationRepository {
    override suspend fun clearAccount() {}
    override suspend fun signInWithEmailAndPassword(email: String, password: String): SignInError? = null
    override suspend fun saveAccountToLocalStorage(email: String, password: String) {}
    override suspend fun checkUserExists(email: String): SignInState = SignInState(true, null)
    override suspend fun checkLocalAccount(): Credentials? = localCredentials
    override suspend fun handleSignInGoogleResult(credential: Any): String? = null
    override suspend fun signUpWithEmailAndPassword(email: String, password: String): Result<Unit> = Result.success(Unit)
    override suspend fun fetchSignInMethodsForEmail(email: String): EmailExistResult = EmailExistResult(false, "")
    override suspend fun sendPasswordResetEmail(email: String): Boolean = true
    override suspend fun saveSignUpInformation(userInstance: UserInstance): Boolean = true
}

private class FakeSettingsRepository(
    var twoFAVerified: Boolean = false
) : SettingsRepository {
    override suspend fun reAuthenticate(currentUserEmail: String, currentPassword: String): Boolean = true
    override suspend fun updateVerify2FASuccess() {}
    override suspend fun get2FAVerifiedStatus(): Boolean = twoFAVerified
    override suspend fun delete2FAStatusInLocal() {}
    override fun observeSessionStatus(userId: String, sessionId: String, onLoggedOut: () -> Unit) {}
    override fun stopObserveSessionStatus() {}
}

private class FakeUserRepository(
    private val currentUserUid: String? = null,
    private val usersById: Map<String, UserDTO?> = emptyMap()
) : UserRepository {
    override suspend fun getUser(userId: String, isCurrentUser: Boolean): UserDTO? = usersById[userId]
    override suspend fun getCurrentUserUid(): String? = currentUserUid
    override suspend fun searchUserByName(name: String): List<UserDTO>? = null
}

@OptIn(ExperimentalCoroutinesApi::class)
class RouterViewModelTest {

    private fun buildViewModel(
        dispatcher: kotlinx.coroutines.CoroutineDispatcher,
        authRepo: FakeAuthenticationRepository = FakeAuthenticationRepository(),
        settingsRepo: FakeSettingsRepository = FakeSettingsRepository(),
        userRepo: FakeUserRepository = FakeUserRepository()
    ): RouterViewModel {
        return RouterViewModel(
            checkLocalAccountUseCase = CheckLocalAccountUseCase(authRepo),
            getCurrentUserUidUseCase = GetCurrentUserUidUseCase(userRepo),
            getUserUseCase = GetUserUseCase(userRepo),
            get2FAVerifiedStatusUseCase = Get2FAVerifiedStatusUseCase(settingsRepo),
            ioDispatcher = dispatcher
        )
    }

    @Test
    fun `no local account routes to sign in`() = runTest {
        val dispatcher = StandardTestDispatcher(testScheduler)
        Dispatchers.setMain(dispatcher)
        val vm = buildViewModel(dispatcher, authRepo = FakeAuthenticationRepository(localCredentials = null))
        advanceUntilIdle()
        Dispatchers.resetMain()

        assertEquals(AuthRouteNames.SignIn.SCREEN_NAME, vm.startDestination.value)
    }

    @Test
    fun `local account with 2FA already verified routes to home`() = runTest {
        val dispatcher = StandardTestDispatcher(testScheduler)
        Dispatchers.setMain(dispatcher)
        val vm = buildViewModel(
            dispatcher,
            authRepo = FakeAuthenticationRepository(localCredentials = Credentials("a@b.com", "pw")),
            settingsRepo = FakeSettingsRepository(twoFAVerified = true)
        )
        advanceUntilIdle()
        Dispatchers.resetMain()

        assertEquals(HomeNavGraph.HOME_SCREEN_NAME, vm.startDestination.value)
    }

    @Test
    fun `local account not verified with no current uid routes to sign in`() = runTest {
        val dispatcher = StandardTestDispatcher(testScheduler)
        Dispatchers.setMain(dispatcher)
        val vm = buildViewModel(
            dispatcher,
            authRepo = FakeAuthenticationRepository(localCredentials = Credentials("a@b.com", "pw")),
            settingsRepo = FakeSettingsRepository(twoFAVerified = false),
            userRepo = FakeUserRepository(currentUserUid = null)
        )
        advanceUntilIdle()
        Dispatchers.resetMain()

        assertEquals(AuthRouteNames.SignIn.SCREEN_NAME, vm.startDestination.value)
    }

    @Test
    fun `local account not verified with uid but no user routes to sign in`() = runTest {
        val dispatcher = StandardTestDispatcher(testScheduler)
        Dispatchers.setMain(dispatcher)
        val vm = buildViewModel(
            dispatcher,
            authRepo = FakeAuthenticationRepository(localCredentials = Credentials("a@b.com", "pw")),
            settingsRepo = FakeSettingsRepository(twoFAVerified = false),
            userRepo = FakeUserRepository(currentUserUid = "u1", usersById = emptyMap())
        )
        advanceUntilIdle()
        Dispatchers.resetMain()

        assertEquals(AuthRouteNames.SignIn.SCREEN_NAME, vm.startDestination.value)
    }

    @Test
    fun `user with twoFAEnabled routes to verify otp screen`() = runTest {
        val dispatcher = StandardTestDispatcher(testScheduler)
        Dispatchers.setMain(dispatcher)
        val dto = UserDTO(uid = "u1", name = "Alice", twoFAEnabled = true)
        val vm = buildViewModel(
            dispatcher,
            authRepo = FakeAuthenticationRepository(localCredentials = Credentials("a@b.com", "pw")),
            settingsRepo = FakeSettingsRepository(twoFAVerified = false),
            userRepo = FakeUserRepository(currentUserUid = "u1", usersById = mapOf("u1" to dto))
        )
        advanceUntilIdle()
        Dispatchers.resetMain()

        assertEquals("VerifyOTPScreen", vm.startDestination.value)
        assertEquals("Alice", vm.currentUser.value.name)
    }

    @Test
    fun `user without twoFAEnabled routes to home`() = runTest {
        val dispatcher = StandardTestDispatcher(testScheduler)
        Dispatchers.setMain(dispatcher)
        val dto = UserDTO(uid = "u1", name = "Bob", twoFAEnabled = false)
        val vm = buildViewModel(
            dispatcher,
            authRepo = FakeAuthenticationRepository(localCredentials = Credentials("a@b.com", "pw")),
            settingsRepo = FakeSettingsRepository(twoFAVerified = false),
            userRepo = FakeUserRepository(currentUserUid = "u1", usersById = mapOf("u1" to dto))
        )
        advanceUntilIdle()
        Dispatchers.resetMain()

        assertEquals(HomeNavGraph.HOME_SCREEN_NAME, vm.startDestination.value)
    }
}
