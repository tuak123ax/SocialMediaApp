package com.minhtu.firesocialmedia.presentation.signin

import com.minhtu.firesocialmedia.auth.data.remote.dto.user.UserDTO
import com.minhtu.firesocialmedia.data.remote.dto.settings.auth.IpInfoResponseDTO
import com.minhtu.firesocialmedia.data.remote.service.database.AuthDatabaseService
import com.minhtu.firesocialmedia.data.remote.service.security.IpInfoRemoteDataSource
import com.minhtu.firesocialmedia.domain.entity.crypto.Credentials
import com.minhtu.firesocialmedia.domain.entity.forgotpassword.EmailExistResult
import com.minhtu.firesocialmedia.domain.entity.signin.SignInState
import com.minhtu.firesocialmedia.domain.entity.user.auth.UserInstance
import com.minhtu.firesocialmedia.domain.error.signin.SignInError
import com.minhtu.firesocialmedia.domain.repository.AuthenticationRepository
import com.minhtu.firesocialmedia.domain.repository.auth.UserRepository
import com.minhtu.firesocialmedia.domain.usecases.common.auth.GetCurrentUserUidUseCase
import com.minhtu.firesocialmedia.domain.usecases.common.auth.GetUserUseCase
import com.minhtu.firesocialmedia.domain.usecases.signin.CheckLocalAccountUseCase
import com.minhtu.firesocialmedia.domain.usecases.signin.CheckUserExistsUseCase
import com.minhtu.firesocialmedia.domain.usecases.signin.HandleSignInGoogleResultUseCase
import com.minhtu.firesocialmedia.domain.usecases.signin.RememberPasswordUseCase
import com.minhtu.firesocialmedia.domain.usecases.signin.SaveLoginActivityInfoUseCase
import com.minhtu.firesocialmedia.domain.usecases.signin.SignInUseCase
import com.minhtu.firesocialmedia.testutil.fakeIpInfoRemoteDataSource
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestCoroutineScheduler
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

private class SignInFakeAuthRepository : AuthenticationRepository {
    var signInError: SignInError? = null
    var signInInvokedWithEmail: String? = null
    var rememberInvokedWith: Pair<String, String>? = null
    var checkUserExistsResult: SignInState = SignInState(true, null)
    var localCredentials: Credentials? = null

    override suspend fun clearAccount() {}
    override suspend fun signInWithEmailAndPassword(email: String, password: String): SignInError? {
        signInInvokedWithEmail = email
        return signInError
    }
    override suspend fun saveAccountToLocalStorage(email: String, password: String) {
        rememberInvokedWith = email to password
    }
    override suspend fun checkUserExists(email: String): SignInState = checkUserExistsResult
    override suspend fun checkLocalAccount(): Credentials? = localCredentials
    override suspend fun handleSignInGoogleResult(credential: Any): String? = null
    override suspend fun signUpWithEmailAndPassword(email: String, password: String): Result<Unit> = Result.success(Unit)
    override suspend fun fetchSignInMethodsForEmail(email: String): EmailExistResult = EmailExistResult(false, "")
    override suspend fun sendPasswordResetEmail(email: String): Boolean = true
    override suspend fun saveSignUpInformation(userInstance: UserInstance): Boolean = true
}

private class SignInFakeUserRepository(
    private val currentUserUid: String? = null,
    private val user: UserDTO? = null
) : UserRepository {
    override suspend fun getUser(userId: String, isCurrentUser: Boolean): UserDTO? = user
    override suspend fun getCurrentUserUid(): String? = currentUserUid
}

private class SignInFakeAuthDatabaseService : AuthDatabaseService {
    var savedLoginActivityForUserId: String? = null
    override suspend fun saveLoginActivityInfo(
        userId: String,
        locationInfo: IpInfoResponseDTO,
        historyPath: String,
        loginHistoryPath: String
    ) {
        savedLoginActivityForUserId = userId
    }
    override suspend fun getUser(userId: String): UserDTO? = null
    override suspend fun saveSignUpInformation(user: UserDTO): Boolean = true
}

@OptIn(ExperimentalCoroutinesApi::class)
class SignInViewModelTest {

    private fun buildViewModel(
        scheduler: TestCoroutineScheduler,
        authRepo: SignInFakeAuthRepository = SignInFakeAuthRepository(),
        userRepo: SignInFakeUserRepository = SignInFakeUserRepository(),
        authDb: SignInFakeAuthDatabaseService = SignInFakeAuthDatabaseService()
    ): Pair<SignInViewModel, Triple<SignInFakeAuthRepository, SignInFakeUserRepository, SignInFakeAuthDatabaseService>> {
        val vm = SignInViewModel(
            SignInUseCase(authRepo),
            RememberPasswordUseCase(authRepo),
            CheckUserExistsUseCase(authRepo),
            CheckLocalAccountUseCase(authRepo),
            HandleSignInGoogleResultUseCase(authRepo),
            GetCurrentUserUidUseCase(userRepo),
            GetUserUseCase(userRepo),
            SaveLoginActivityInfoUseCase(authDb, fakeIpInfoRemoteDataSource()),
            StandardTestDispatcher(scheduler)
        )
        return vm to Triple(authRepo, userRepo, authDb)
    }

    @Test
    fun `signIn with empty email and password emits DataEmpty error`() = runTest {
        val (vm, _) = buildViewModel(testScheduler)
        vm.signIn { }
        advanceUntilIdle()

        val state = vm.signInState.value
        assertEquals(false, state.signInStatus)
        assertEquals(SignInError.DataEmpty, state.error)
    }

    @Test
    fun `signIn success delegates to checkUserExists and emits authenticated state`() = runTest {
        val authRepo = SignInFakeAuthRepository().apply {
            checkUserExistsResult = SignInState(true, null)
        }
        val (vm, _) = buildViewModel(testScheduler, authRepo = authRepo)
        vm.updateEmail("user@example.com")
        vm.updatePassword("123456")

        vm.signIn { }
        advanceUntilIdle()

        val state = vm.signInState.value
        assertEquals(true, state.signInStatus)
        assertNull(state.error)
    }

    @Test
    fun `signIn failure surfaces use case error in signInState`() = runTest {
        val authRepo = SignInFakeAuthRepository().apply { signInError = SignInError.WrongPassword }
        val (vm, _) = buildViewModel(testScheduler, authRepo = authRepo)
        vm.updateEmail("user@example.com")
        vm.updatePassword("wrongPassword")

        vm.signIn { }
        advanceUntilIdle()

        val state = vm.signInState.value
        assertEquals(false, state.signInStatus)
        assertEquals(SignInError.WrongPassword, state.error)
    }

    @Test
    fun `signIn with rememberPassword on triggers remember use case`() = runTest {
        val authRepo = SignInFakeAuthRepository()
        val (vm, fakes) = buildViewModel(testScheduler, authRepo = authRepo)
        vm.updateEmail("user@example.com")
        vm.updatePassword("123456")
        vm.updateRememberPassword(true)

        vm.signIn { }
        advanceUntilIdle()

        val invoked = fakes.first.rememberInvokedWith
        assertNotNull(invoked)
        assertEquals("user@example.com", invoked.first)
        assertEquals("123456", invoked.second)
    }

    @Test
    fun `signIn lowercases the email before invoking use case`() = runTest {
        val authRepo = SignInFakeAuthRepository()
        val (vm, fakes) = buildViewModel(testScheduler, authRepo = authRepo)
        vm.updateEmail("USER@EXAMPLE.COM")
        vm.updatePassword("123456")

        vm.signIn { }
        advanceUntilIdle()

        assertEquals("user@example.com", fakes.first.signInInvokedWithEmail)
    }

    @Test
    fun `resetSignInStatus clears signInState`() = runTest {
        val (vm, _) = buildViewModel(testScheduler)
        vm.signIn { } // triggers DataEmpty
        advanceUntilIdle()
        assertEquals(SignInError.DataEmpty, vm.signInState.value.error)

        vm.resetSignInStatus()

        assertEquals(false, vm.signInState.value.signInStatus)
        assertNull(vm.signInState.value.error)
    }

    @Test
    fun `checkLocalAccount populates email and password when credentials exist`() = runTest {
        val authRepo = SignInFakeAuthRepository().apply { localCredentials = Credentials("a@b.com", "pw") }
        val (vm, _) = buildViewModel(testScheduler, authRepo = authRepo)
        vm.checkLocalAccount()
        advanceUntilIdle()

        assertEquals("a@b.com", vm.email.value)
        assertEquals("pw", vm.password.value)
        assertNotNull(vm.localCredentials.value)
    }

    @Test
    fun `check2FAStatus updates state from fetched user`() = runTest {
        val user = UserDTO(uid = "u1", name = "Alice", email = "a@b.com", twoFAEnabled = true)
        val userRepo = SignInFakeUserRepository(currentUserUid = "u1", user = user)
        val (vm, fakes) = buildViewModel(testScheduler, userRepo = userRepo)
        vm.check2FAStatus()
        advanceUntilIdle()

        assertEquals(true, vm.check2FAStatus.value)
        assertEquals("u1", vm.currentUser.value?.uid)
        assertEquals("u1", fakes.third.savedLoginActivityForUserId)
    }

    @Test
    fun `reset clears all in-memory fields`() = runTest {
        val (vm, _) = buildViewModel(testScheduler)
        vm.updateEmail("a@b.com")
        vm.updatePassword("123")
        vm.updateRememberPassword(true)

        vm.reset()

        assertEquals("", vm.email.value)
        assertEquals("", vm.password.value)
        assertEquals(false, vm.rememberPassword.value)
        assertNull(vm.currentUser.value)
    }
}
