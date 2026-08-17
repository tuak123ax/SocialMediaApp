package com.minhtu.firesocialmedia.presentation.forgotpassword

import com.minhtu.firesocialmedia.constants.auth.Constants
import com.minhtu.firesocialmedia.domain.entity.crypto.Credentials
import com.minhtu.firesocialmedia.domain.entity.forgotpassword.EmailExistResult
import com.minhtu.firesocialmedia.domain.entity.signin.SignInState
import com.minhtu.firesocialmedia.domain.entity.user.auth.UserInstance
import com.minhtu.firesocialmedia.domain.error.signin.SignInError
import com.minhtu.firesocialmedia.domain.repository.AuthenticationRepository
import com.minhtu.firesocialmedia.domain.usecases.forgotpassword.CheckIfEmailExistsUseCase
import com.minhtu.firesocialmedia.domain.usecases.forgotpassword.SendEmailResetPasswordUseCase
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

private class ForgotFakeAuthRepository : AuthenticationRepository {
    var emailExistResult: EmailExistResult = EmailExistResult(false, Constants.EMAIL_NOT_EXISTED)
    var sendResetResult: Boolean = false

    override suspend fun clearAccount() {}
    override suspend fun signInWithEmailAndPassword(email: String, password: String): SignInError? = null
    override suspend fun saveAccountToLocalStorage(email: String, password: String) {}
    override suspend fun checkUserExists(email: String): SignInState = SignInState(false, null)
    override suspend fun checkLocalAccount(): Credentials? = null
    override suspend fun handleSignInGoogleResult(credential: Any): String? = null
    override suspend fun signUpWithEmailAndPassword(email: String, password: String): Result<Unit> = Result.success(Unit)
    override suspend fun fetchSignInMethodsForEmail(email: String): EmailExistResult = emailExistResult
    override suspend fun sendPasswordResetEmail(email: String): Boolean = sendResetResult
    override suspend fun saveSignUpInformation(userInstance: UserInstance): Boolean = true
}

@OptIn(ExperimentalCoroutinesApi::class)
class ForgotPasswordViewModelTest {
    private lateinit var repo: ForgotFakeAuthRepository

    @BeforeTest
    fun setup() {
        repo = ForgotFakeAuthRepository()
    }

    private fun vm(dispatcher: kotlinx.coroutines.CoroutineDispatcher) = ForgotPasswordViewModel(
        CheckIfEmailExistsUseCase(repo),
        SendEmailResetPasswordUseCase(repo),
        dispatcher
    )

    @Test
    fun `test check email exists with empty email trigger fail flow`() = runTest {
        val dispatcher = StandardTestDispatcher(testScheduler)
        val viewModel = vm(dispatcher)
        viewModel.updateEmail("")
        viewModel.checkIfEmailExists()
        advanceUntilIdle()

        val status = viewModel.emailExisted.value
        assertEquals(false, status?.exist)
        assertEquals(Constants.EMAIL_EMPTY, status?.message)
    }

    @Test
    fun `test check email exists with email not exist trigger fail flow`() = runTest {
        val dispatcher = StandardTestDispatcher(testScheduler)
        val viewModel = vm(dispatcher)
        viewModel.updateEmail("test@gmail.com")
        repo.emailExistResult = EmailExistResult(false, Constants.EMAIL_NOT_EXISTED)

        viewModel.checkIfEmailExists()
        advanceUntilIdle()

        val status = viewModel.emailExisted.value
        assertEquals(false, status?.exist)
        assertEquals(Constants.EMAIL_NOT_EXISTED, status?.message)
    }

    @Test
    fun `test check email exists with server error trigger fail flow`() = runTest {
        val dispatcher = StandardTestDispatcher(testScheduler)
        val viewModel = vm(dispatcher)
        viewModel.updateEmail("test@gmail.com")
        repo.emailExistResult = EmailExistResult(false, Constants.EMAIL_SERVER_ERROR)

        viewModel.checkIfEmailExists()
        advanceUntilIdle()

        val status = viewModel.emailExisted.value
        assertEquals(false, status?.exist)
        assertEquals(Constants.EMAIL_SERVER_ERROR, status?.message)
    }

    @Test
    fun `test check email exists trigger success flow`() = runTest {
        val dispatcher = StandardTestDispatcher(testScheduler)
        val viewModel = vm(dispatcher)
        viewModel.updateEmail("test@gmail.com")
        repo.emailExistResult = EmailExistResult(true, Constants.EMAIL_EXISTED)

        viewModel.checkIfEmailExists()
        advanceUntilIdle()

        val status = viewModel.emailExisted.value
        assertEquals(true, status?.exist)
        assertEquals(Constants.EMAIL_EXISTED, status?.message)
    }

    @Test
    fun `test send email reset password trigger success flow`() = runTest {
        val dispatcher = StandardTestDispatcher(testScheduler)
        val viewModel = vm(dispatcher)
        viewModel.updateEmail("test@gmail.com")
        repo.sendResetResult = true

        viewModel.sendEmailResetPassword()
        advanceUntilIdle()

        assertEquals(true, viewModel.emailSent.value)
    }

    @Test
    fun `test send email reset password trigger fail flow`() = runTest {
        val dispatcher = StandardTestDispatcher(testScheduler)
        val viewModel = vm(dispatcher)
        viewModel.updateEmail("test@gmail.com")
        repo.sendResetResult = false

        viewModel.sendEmailResetPassword()
        advanceUntilIdle()

        assertEquals(false, viewModel.emailSent.value)
    }

    @Test
    fun `resetEmailExistStatus sets state to null`() = runTest {
        val dispatcher = StandardTestDispatcher(testScheduler)
        val viewModel = vm(dispatcher)
        viewModel.updateEmail("test@gmail.com")
        repo.emailExistResult = EmailExistResult(true, Constants.EMAIL_EXISTED)
        viewModel.checkIfEmailExists()
        advanceUntilIdle()
        assertEquals(true, viewModel.emailExisted.value?.exist)

        viewModel.resetEmailExistStatus()
        assertNull(viewModel.emailExisted.value)
    }

    @Test
    fun `resetEmailResetPassword sets state to null`() = runTest {
        val dispatcher = StandardTestDispatcher(testScheduler)
        val viewModel = vm(dispatcher)
        viewModel.updateEmail("test@gmail.com")
        repo.sendResetResult = true
        viewModel.sendEmailResetPassword()
        advanceUntilIdle()
        assertEquals(true, viewModel.emailSent.value)

        viewModel.resetEmailResetPassword()
        assertNull(viewModel.emailSent.value)
    }
}
