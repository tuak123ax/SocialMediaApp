package com.minhtu.firesocialmedia

import com.minhtu.firesocialmedia.constants.Constants
import com.minhtu.firesocialmedia.domain.repository.AuthenticationRepository
import com.minhtu.firesocialmedia.domain.usecases.signup.SignUpUseCase
import com.minhtu.firesocialmedia.presentation.signup.SignUpViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

class SignUpFakeAuthRepository : AuthenticationRepository {
    var signUpResult: Result<Unit> = Result.success(Unit)
    override suspend fun signInWithEmailAndPassword(email: String, password: String) = null
    override suspend fun saveAccountToLocalStorage(email: String, password: String) {}
    override suspend fun checkUserExists(email: String) = com.minhtu.firesocialmedia.domain.entity.signin.SignInState(false, null)
    override suspend fun checkLocalAccount() = null
    override suspend fun handleSignInGoogleResult(credential: Any) = null
    override suspend fun signUpWithEmailAndPassword(email: String, password: String) = signUpResult
    override suspend fun fetchSignInMethodsForEmail(email: String) = com.minhtu.firesocialmedia.domain.entity.forgotpassword.EmailExistResult(false, "")
    override suspend fun sendPasswordResetEmail(email: String) = true
    override suspend fun clearAccount() {}
    override suspend fun saveSignUpInformation(userInstance: com.minhtu.firesocialmedia.domain.entity.user.UserInstance) = true
}

class SignUpViewModelTest {
    private lateinit var repo: SignUpFakeAuthRepository

    @OptIn(ExperimentalCoroutinesApi::class)
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
        assertEquals(Constants.DATA_EMPTY, signUpStatus.message)
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
        assertEquals(Constants.DATA_EMPTY, signUpStatus.message)
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
        assertEquals(Constants.DATA_EMPTY, signUpStatus.message)
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
        assertEquals(Constants.DATA_EMPTY, signUpStatus.message)
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
        assertEquals(Constants.PASSWORD_MISMATCH, signUpStatus.message)
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
        assertEquals(Constants.PASSWORD_SHORT, signUpStatus.message)
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
}