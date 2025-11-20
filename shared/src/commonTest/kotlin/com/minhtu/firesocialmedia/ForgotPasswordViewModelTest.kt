package com.minhtu.firesocialmedia

import com.minhtu.firesocialmedia.constants.Constants
import com.minhtu.firesocialmedia.domain.entity.forgotpassword.EmailExistResult
import com.minhtu.firesocialmedia.domain.repository.AuthenticationRepository
import com.minhtu.firesocialmedia.domain.usecases.forgotpassword.CheckIfEmailExistsUseCase
import com.minhtu.firesocialmedia.domain.usecases.forgotpassword.SendEmailResetPasswordUseCase
import com.minhtu.firesocialmedia.presentation.forgotpassword.ForgotPasswordViewModel
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

private class ForgotFakeAuthRepository : AuthenticationRepository {
    var emailExistResult: EmailExistResult = EmailExistResult(false, Constants.EMAIL_NOT_EXISTED)
    var sendResetResult: Boolean = false

    override suspend fun signInWithEmailAndPassword(email: String, password: String) = null
    override suspend fun saveAccountToLocalStorage(email: String, password: String) {}
    override suspend fun checkUserExists(email: String) = com.minhtu.firesocialmedia.domain.entity.signin.SignInState(false, null)
    override suspend fun checkLocalAccount() = null
    override suspend fun handleSignInGoogleResult(credential: Any) = null
    override suspend fun signUpWithEmailAndPassword(email: String, password: String) = Result.success(Unit)
    override suspend fun fetchSignInMethodsForEmail(email: String) = emailExistResult
    override suspend fun sendPasswordResetEmail(email: String) = sendResetResult
    override suspend fun clearAccount() {}
    override suspend fun saveSignUpInformation(userInstance: com.minhtu.firesocialmedia.domain.entity.user.UserInstance) = true
}

@OptIn(ExperimentalCoroutinesApi::class)
class ForgotPasswordViewModelTest {
    private val testDispatcher = StandardTestDispatcher()
    private lateinit var repo: ForgotFakeAuthRepository
    private lateinit var viewModel: ForgotPasswordViewModel

    @BeforeTest
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        repo = ForgotFakeAuthRepository()
        viewModel = ForgotPasswordViewModel(
            CheckIfEmailExistsUseCase(repo),
            SendEmailResetPasswordUseCase(repo),
            testDispatcher
        )
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `test check email exists with empty email trigger fail flow`() = runTest(testDispatcher) {
        viewModel.updateEmail("")
        viewModel.checkIfEmailExists()
        advanceUntilIdle()

        val status = viewModel.emailExisted.value
        assertEquals(false, status?.exist)
        assertEquals(Constants.EMAIL_EMPTY, status?.message)
    }

    @Test
    fun `test check email exists with email not exist trigger fail flow`() = runTest(testDispatcher) {
        viewModel.updateEmail("test@gmail.com")
        repo.emailExistResult = EmailExistResult(false, Constants.EMAIL_NOT_EXISTED)

        viewModel.checkIfEmailExists()
        advanceUntilIdle()

        val status = viewModel.emailExisted.value
        assertEquals(false, status?.exist)
        assertEquals(Constants.EMAIL_NOT_EXISTED, status?.message)
    }

    @Test
    fun `test check email exists with server error trigger fail flow`() = runTest(testDispatcher) {
        viewModel.updateEmail("test@gmail.com")
        repo.emailExistResult = EmailExistResult(false, Constants.EMAIL_SERVER_ERROR)

        viewModel.checkIfEmailExists()
        advanceUntilIdle()

        val status = viewModel.emailExisted.value
        assertEquals(false, status?.exist)
        assertEquals(Constants.EMAIL_SERVER_ERROR, status?.message)
    }

    @Test
    fun `test check email exists trigger success flow`() = runTest(testDispatcher) {
        viewModel.updateEmail("test@gmail.com")
        repo.emailExistResult = EmailExistResult(true, Constants.EMAIL_EXISTED)

        viewModel.checkIfEmailExists()
        advanceUntilIdle()

        val status = viewModel.emailExisted.value
        assertEquals(true, status?.exist)
        assertEquals(Constants.EMAIL_EXISTED, status?.message)
    }

    @Test
    fun `test send email reset password trigger success flow`() = runTest(testDispatcher) {
        viewModel.updateEmail("test@gmail.com")
        repo.sendResetResult = true

        viewModel.sendEmailResetPassword()
        advanceUntilIdle()

        val status = viewModel.emailSent.value
        assertEquals(true, status)
    }

    @Test
    fun `test send email reset password trigger fail flow`() = runTest(testDispatcher) {
        viewModel.updateEmail("test@gmail.com")
        repo.sendResetResult = false

        viewModel.sendEmailResetPassword()
        advanceUntilIdle()

        val status = viewModel.emailSent.value
        assertEquals(false, status)
    }
}