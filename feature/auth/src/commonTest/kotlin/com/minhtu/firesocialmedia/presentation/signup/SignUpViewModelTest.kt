package com.minhtu.firesocialmedia.presentation.signup

import com.minhtu.firesocialmedia.domain.entity.crypto.Credentials
import com.minhtu.firesocialmedia.domain.entity.forgotpassword.EmailExistResult
import com.minhtu.firesocialmedia.domain.entity.signin.SignInState
import com.minhtu.firesocialmedia.domain.entity.user.auth.UserInstance
import com.minhtu.firesocialmedia.domain.error.signin.SignInError
import com.minhtu.firesocialmedia.domain.error.signup.SignUpError
import com.minhtu.firesocialmedia.domain.repository.AuthenticationRepository
import com.minhtu.firesocialmedia.domain.usecases.signup.SignUpUseCase
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

private class SignUpFakeAuthRepository : AuthenticationRepository {
    var signUpResult: Result<Unit> = Result.success(Unit)
    override suspend fun clearAccount() {}
    override suspend fun signInWithEmailAndPassword(email: String, password: String): SignInError? = null
    override suspend fun saveAccountToLocalStorage(email: String, password: String) {}
    override suspend fun checkUserExists(email: String): SignInState = SignInState(false, null)
    override suspend fun checkLocalAccount(): Credentials? = null
    override suspend fun handleSignInGoogleResult(credential: Any): String? = null
    override suspend fun signUpWithEmailAndPassword(email: String, password: String): Result<Unit> = signUpResult
    override suspend fun fetchSignInMethodsForEmail(email: String): EmailExistResult = EmailExistResult(false, "")
    override suspend fun sendPasswordResetEmail(email: String): Boolean = true
    override suspend fun saveSignUpInformation(userInstance: UserInstance): Boolean = true
}

class SignUpViewModelTest {
    private lateinit var repo: SignUpFakeAuthRepository

    @BeforeTest
    fun setup() {
        repo = SignUpFakeAuthRepository()
    }

    private fun vm(dispatcher: CoroutineDispatcher): SignUpViewModel = SignUpViewModel(SignUpUseCase(repo), dispatcher)

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `signUp with all fields blank trigger fail flow`() = runTest {
        val dispatcher = StandardTestDispatcher(testScheduler)
        val signUpViewModel = vm(dispatcher)
        signUpViewModel.email = ""
        signUpViewModel.password = ""
        signUpViewModel.confirmPassword = ""
        signUpViewModel.signUp()

        advanceUntilIdle()
        val signUpStatus = signUpViewModel.signUpStatus.value
        assertEquals(false, signUpStatus.signUpStatus)
        assertEquals(SignUpError.DataEmptyError.message, signUpStatus.message)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `signUp with email blank trigger fail flow`() = runTest {
        val dispatcher = StandardTestDispatcher(testScheduler)
        val signUpViewModel = vm(dispatcher)
        signUpViewModel.email = ""
        signUpViewModel.password = "123321"
        signUpViewModel.confirmPassword = "123321"
        signUpViewModel.signUp()

        advanceUntilIdle()
        val signUpStatus = signUpViewModel.signUpStatus.value
        assertEquals(false, signUpStatus.signUpStatus)
        assertEquals(SignUpError.DataEmptyError.message, signUpStatus.message)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `signUp with password blank trigger fail flow`() = runTest {
        val dispatcher = StandardTestDispatcher(testScheduler)
        val signUpViewModel = vm(dispatcher)
        signUpViewModel.email = "test@gmail.com"
        signUpViewModel.password = ""
        signUpViewModel.confirmPassword = "123321"
        signUpViewModel.signUp()

        advanceUntilIdle()
        val signUpStatus = signUpViewModel.signUpStatus.value
        assertEquals(false, signUpStatus.signUpStatus)
        assertEquals(SignUpError.DataEmptyError.message, signUpStatus.message)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `signUp with confirm password blank trigger fail flow`() = runTest {
        val dispatcher = StandardTestDispatcher(testScheduler)
        val signUpViewModel = vm(dispatcher)
        signUpViewModel.email = "test@gmail.com"
        signUpViewModel.password = "123321"
        signUpViewModel.confirmPassword = ""
        signUpViewModel.signUp()

        advanceUntilIdle()
        val signUpStatus = signUpViewModel.signUpStatus.value
        assertEquals(false, signUpStatus.signUpStatus)
        assertEquals(SignUpError.DataEmptyError.message, signUpStatus.message)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `signUp with different password and confirm password trigger fail flow`() = runTest {
        val dispatcher = StandardTestDispatcher(testScheduler)
        val signUpViewModel = vm(dispatcher)
        signUpViewModel.email = "test@gmail.com"
        signUpViewModel.password = "123321"
        signUpViewModel.confirmPassword = "123456"
        signUpViewModel.signUp()

        advanceUntilIdle()
        val signUpStatus = signUpViewModel.signUpStatus.value
        assertEquals(false, signUpStatus.signUpStatus)
        assertEquals(SignUpError.PasswordMismatchError.message, signUpStatus.message)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `signUp with short password trigger fail flow`() = runTest {
        val dispatcher = StandardTestDispatcher(testScheduler)
        val signUpViewModel = vm(dispatcher)
        signUpViewModel.email = "test@gmail.com"
        signUpViewModel.password = "123"
        signUpViewModel.confirmPassword = "123"
        signUpViewModel.signUp()

        advanceUntilIdle()
        val signUpStatus = signUpViewModel.signUpStatus.value
        assertEquals(false, signUpStatus.signUpStatus)
        assertEquals(SignUpError.PasswordShortError.message, signUpStatus.message)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `signUp with correct info trigger success flow`() = runTest {
        val dispatcher = StandardTestDispatcher(testScheduler)
        val signUpViewModel = vm(dispatcher)
        signUpViewModel.email = "test@gmail.com"
        signUpViewModel.password = "123321"
        signUpViewModel.confirmPassword = "123321"

        repo.signUpResult = Result.success(Unit)
        signUpViewModel.signUp()

        advanceUntilIdle()
        val signUpStatus = signUpViewModel.signUpStatus.value
        assertEquals(true, signUpStatus.signUpStatus)
        assertEquals("", signUpStatus.message)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `signUp with correct info but fail on server trigger fail flow`() = runTest {
        val dispatcher = StandardTestDispatcher(testScheduler)
        val signUpViewModel = vm(dispatcher)
        signUpViewModel.updateEmail("test@gmail.com")
        signUpViewModel.updatePassword("123321")
        signUpViewModel.updateConfirmPassword("123321")

        repo.signUpResult = Result.failure(Exception("Sign up failed"))
        signUpViewModel.signUp()

        advanceUntilIdle()
        val signUpStatus = signUpViewModel.signUpStatus.value
        assertEquals(false, signUpStatus.signUpStatus)
        assertEquals("Sign up failed", signUpStatus.message)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `signUp fail with no message falls back to unknown error message`() = runTest {
        val dispatcher = StandardTestDispatcher(testScheduler)
        val signUpViewModel = vm(dispatcher)
        signUpViewModel.updateEmail("test@gmail.com")
        signUpViewModel.updatePassword("123321")
        signUpViewModel.updateConfirmPassword("123321")

        repo.signUpResult = Result.failure(Exception())
        signUpViewModel.signUp()

        advanceUntilIdle()
        val signUpStatus = signUpViewModel.signUpStatus.value
        assertEquals(false, signUpStatus.signUpStatus)
        assertEquals(SignUpError.Unknown("Sign up failed. Something went wrong!").message, signUpStatus.message)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `resetSignUpStatus clears signUpStatus`() = runTest {
        val dispatcher = StandardTestDispatcher(testScheduler)
        val signUpViewModel = vm(dispatcher)
        signUpViewModel.email = ""
        signUpViewModel.password = ""
        signUpViewModel.confirmPassword = ""
        signUpViewModel.signUp()
        advanceUntilIdle()
        assertEquals(false, signUpViewModel.signUpStatus.value.signUpStatus)

        signUpViewModel.resetSignUpStatus()

        assertEquals(false, signUpViewModel.signUpStatus.value.signUpStatus)
        assertEquals("", signUpViewModel.signUpStatus.value.message)
    }
}
