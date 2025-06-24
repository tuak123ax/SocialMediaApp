package com.minhtu.firesocialmedia

import com.minhtu.firesocialmedia.constants.Constants
import com.minhtu.firesocialmedia.forgotpassword.ForgotPasswordViewModel
import com.minhtu.firesocialmedia.utils.Utils
import io.mockative.any
import io.mockative.eq
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

@OptIn(ExperimentalCoroutinesApi::class)
class ForgotPasswordViewModelTest {
    private val testDispatcher = StandardTestDispatcher()
    private lateinit var platformContext: PlatformContextMock
    private lateinit var authService: AuthServiceMock

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

    @Test
    fun `test check email exists with empty email trigger fail flow`() = runTest(testDispatcher) {
        val forgotPasswordViewModel = ForgotPasswordViewModel(testDispatcher)
        forgotPasswordViewModel.updateEmail("")

        forgotPasswordViewModel.checkIfEmailExists(platformContext)

        advanceUntilIdle()
        val status = forgotPasswordViewModel.emailExisted.value
        assertEquals(false, status?.first)
        assertEquals(Constants.EMAIL_EMPTY, status?.second)
    }

    @Test
    fun `test check email exists with email not exist trigger fail flow`() = runTest(testDispatcher) {
        val forgotPasswordViewModel = ForgotPasswordViewModel(testDispatcher)
        forgotPasswordViewModel.updateEmail("test@gmail.com")

        every { platformContext.auth } returns authService
        every { authService.fetchSignInMethodsForEmail(eq(forgotPasswordViewModel.email), any()) }
            .invokes { args ->
                val callback = args[1] as Utils.Companion.FetchSignInMethodCallback
                callback.onFailure(Pair(false, Constants.EMAIL_NOT_EXISTED))
            }

        forgotPasswordViewModel.checkIfEmailExists(platformContext)

        advanceUntilIdle()
        val status = forgotPasswordViewModel.emailExisted.value
        assertEquals(false, status?.first)
        assertEquals(Constants.EMAIL_NOT_EXISTED, status?.second)
    }

    @Test
    fun `test check email exists with server error trigger fail flow`() = runTest(testDispatcher) {
        val forgotPasswordViewModel = ForgotPasswordViewModel(testDispatcher)
        forgotPasswordViewModel.updateEmail("test@gmail.com")

        every { platformContext.auth } returns authService
        every { authService.fetchSignInMethodsForEmail(eq(forgotPasswordViewModel.email), any()) }
            .invokes { args ->
                val callback = args[1] as Utils.Companion.FetchSignInMethodCallback
                callback.onFailure(Pair(false, Constants.EMAIL_SERVER_ERROR))
            }

        forgotPasswordViewModel.checkIfEmailExists(platformContext)

        advanceUntilIdle()
        val status = forgotPasswordViewModel.emailExisted.value
        assertEquals(false, status?.first)
        assertEquals(Constants.EMAIL_SERVER_ERROR, status?.second)
    }

    @Test
    fun `test check email exists trigger success flow`() = runTest(testDispatcher) {
        val forgotPasswordViewModel = ForgotPasswordViewModel(testDispatcher)
        forgotPasswordViewModel.updateEmail("test@gmail.com")

        every { platformContext.auth } returns authService
        every { authService.fetchSignInMethodsForEmail(eq(forgotPasswordViewModel.email), any()) }
            .invokes { args ->
                val callback = args[1] as Utils.Companion.FetchSignInMethodCallback
                callback.onSuccess(Pair(true, Constants.EMAIL_EXISTED))
            }

        forgotPasswordViewModel.checkIfEmailExists(platformContext)

        advanceUntilIdle()
        val status = forgotPasswordViewModel.emailExisted.value
        assertEquals(true, status?.first)
        assertEquals(Constants.EMAIL_EXISTED, status?.second)
    }

    @Test
    fun `test send email reset password trigger success flow`() = runTest(testDispatcher) {
        val forgotPasswordViewModel = ForgotPasswordViewModel(testDispatcher)
        forgotPasswordViewModel.updateEmail("test@gmail.com")

        every { platformContext.auth } returns authService
        every { authService.sendPasswordResetEmail(eq(forgotPasswordViewModel.email), any()) }
            .invokes { args ->
                val callback = args[1] as Utils.Companion.SendPasswordResetEmailCallback
                callback.onSuccess()
            }

        forgotPasswordViewModel.sendEmailResetPassword(platformContext)

        advanceUntilIdle()
        val status = forgotPasswordViewModel.emailSent.value
        assertEquals(true, status)
    }

    @Test
    fun `test send email reset password trigger fail flow`() = runTest(testDispatcher) {
        val forgotPasswordViewModel = ForgotPasswordViewModel(testDispatcher)
        forgotPasswordViewModel.updateEmail("test@gmail.com")

        every { platformContext.auth } returns authService
        every { authService.sendPasswordResetEmail(eq(forgotPasswordViewModel.email), any()) }
            .invokes { args ->
                val callback = args[1] as Utils.Companion.SendPasswordResetEmailCallback
                callback.onFailure()
            }

        forgotPasswordViewModel.sendEmailResetPassword(platformContext)

        advanceUntilIdle()
        val status = forgotPasswordViewModel.emailSent.value
        assertEquals(false, status)
    }
}