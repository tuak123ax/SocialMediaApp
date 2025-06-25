package com.minhtu.firesocialmedia

import com.minhtu.firesocialmedia.constants.Constants
import com.minhtu.firesocialmedia.di.AuthServiceMock
import com.minhtu.firesocialmedia.di.PlatformContextMock
import com.minhtu.firesocialmedia.presentation.signup.SignUpViewModel
import io.mockative.coEvery
import io.mockative.every
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

class SignUpViewModelTest {
    private val testDispatcher = StandardTestDispatcher()
    private lateinit var platformContext: PlatformContextMock
    private lateinit var authService : AuthServiceMock

    @OptIn(ExperimentalCoroutinesApi::class)
    @BeforeTest
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        platformContext = PlatformContextMock()
        authService = AuthServiceMock()
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `signUp with all fields blank trigger fail flow`() = runTest(testDispatcher) {
        val signUpViewModel = SignUpViewModel(testDispatcher)

        signUpViewModel.email = ""
        signUpViewModel.password = ""
        signUpViewModel.confirmPassword = ""
        signUpViewModel.signUp(platformContext)

        advanceUntilIdle()
        val signUpStatus = signUpViewModel.signUpStatus.value
        assertEquals(false, signUpStatus.signUpStatus)
        assertEquals(Constants.DATA_EMPTY, signUpStatus.message)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `signUp with email blank trigger fail flow`() = runTest(testDispatcher) {
        val signUpViewModel = SignUpViewModel(testDispatcher)

        signUpViewModel.email = ""
        signUpViewModel.password = "123321"
        signUpViewModel.confirmPassword = "123321"
        signUpViewModel.signUp(platformContext)

        advanceUntilIdle()
        val signUpStatus = signUpViewModel.signUpStatus.value
        assertEquals(false, signUpStatus.signUpStatus)
        assertEquals(Constants.DATA_EMPTY, signUpStatus.message)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `signUp with password blank trigger fail flow`() = runTest(testDispatcher) {
        val signUpViewModel = SignUpViewModel(testDispatcher)

        signUpViewModel.email = "test@gmail.com"
        signUpViewModel.password = ""
        signUpViewModel.confirmPassword = "123321"
        signUpViewModel.signUp(platformContext)

        advanceUntilIdle()
        val signUpStatus = signUpViewModel.signUpStatus.value
        assertEquals(false, signUpStatus.signUpStatus)
        assertEquals(Constants.DATA_EMPTY, signUpStatus.message)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `signUp with confirm password blank trigger fail flow`() = runTest(testDispatcher) {
        val signUpViewModel = SignUpViewModel(testDispatcher)

        signUpViewModel.email = "test@gmail.com"
        signUpViewModel.password = "123321"
        signUpViewModel.confirmPassword = ""
        signUpViewModel.signUp(platformContext)

        advanceUntilIdle()
        val signUpStatus = signUpViewModel.signUpStatus.value
        assertEquals(false, signUpStatus.signUpStatus)
        assertEquals(Constants.DATA_EMPTY, signUpStatus.message)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `signUp with different password and confirm password trigger fail flow`() = runTest(testDispatcher) {
        val signUpViewModel = SignUpViewModel(testDispatcher)

        signUpViewModel.email = "test@gmail.com"
        signUpViewModel.password = "123321"
        signUpViewModel.confirmPassword = "123456"
        signUpViewModel.signUp(platformContext)

        advanceUntilIdle()
        val signUpStatus = signUpViewModel.signUpStatus.value
        assertEquals(false, signUpStatus.signUpStatus)
        assertEquals(Constants.PASSWORD_MISMATCH, signUpStatus.message)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `signUp with short password trigger fail flow`() = runTest(testDispatcher) {
        val signUpViewModel = SignUpViewModel(testDispatcher)

        signUpViewModel.email = "test@gmail.com"
        signUpViewModel.password = "123"
        signUpViewModel.confirmPassword = "123"
        signUpViewModel.signUp(platformContext)

        advanceUntilIdle()
        val signUpStatus = signUpViewModel.signUpStatus.value
        assertEquals(false, signUpStatus.signUpStatus)
        assertEquals(Constants.PASSWORD_SHORT, signUpStatus.message)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `signUp with correct info trigger success flow`() = runTest(testDispatcher) {
        val signUpViewModel = SignUpViewModel(testDispatcher)

        signUpViewModel.email = "test@gmail.com"
        signUpViewModel.password = "123321"
        signUpViewModel.confirmPassword = "123321"

        every { platformContext.auth } returns authService
        coEvery { authService.signUpWithEmailAndPassword(signUpViewModel.email, signUpViewModel.password)} returns Result.success(Unit)
        signUpViewModel.signUp(platformContext)

        advanceUntilIdle()
        val signUpStatus = signUpViewModel.signUpStatus.value
        assertEquals(true, signUpStatus.signUpStatus)
        assertEquals("", signUpStatus.message)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `signUp with correct info but fail on server trigger fail flow`() = runTest(testDispatcher) {
        val signUpViewModel = SignUpViewModel(testDispatcher)

        signUpViewModel.updateEmail("test@gmail.com")
        signUpViewModel.updatePassword("123321")
        signUpViewModel.updateConfirmPassword("123321")

        every { platformContext.auth } returns authService
        coEvery { authService.signUpWithEmailAndPassword(signUpViewModel.email, signUpViewModel.password)} returns Result.failure(
            Exception("Sign up failed")
        )
        signUpViewModel.signUp(platformContext)

        advanceUntilIdle()
        val signUpStatus = signUpViewModel.signUpStatus.value
        assertEquals(false, signUpStatus.signUpStatus)
        assertEquals(Constants.SIGNUP_FAIL, signUpStatus.message)
    }
}