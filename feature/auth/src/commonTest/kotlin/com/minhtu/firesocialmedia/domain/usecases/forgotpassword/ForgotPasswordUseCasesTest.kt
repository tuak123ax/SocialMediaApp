package com.minhtu.firesocialmedia.domain.usecases.forgotpassword

import com.minhtu.firesocialmedia.domain.entity.crypto.Credentials
import com.minhtu.firesocialmedia.domain.entity.forgotpassword.EmailExistResult
import com.minhtu.firesocialmedia.domain.entity.signin.SignInState
import com.minhtu.firesocialmedia.domain.entity.user.auth.UserInstance
import com.minhtu.firesocialmedia.domain.error.signin.SignInError
import com.minhtu.firesocialmedia.domain.repository.AuthenticationRepository
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

private class FakeAuthenticationRepository : AuthenticationRepository {
    var emailExistResult: EmailExistResult = EmailExistResult(false, "")
    var sendResetResult: Boolean = false
    var requestedEmail: String? = null

    override suspend fun clearAccount() {}
    override suspend fun signInWithEmailAndPassword(email: String, password: String): SignInError? = null
    override suspend fun saveAccountToLocalStorage(email: String, password: String) {}
    override suspend fun checkUserExists(email: String): SignInState = SignInState(false, null)
    override suspend fun checkLocalAccount(): Credentials? = null
    override suspend fun handleSignInGoogleResult(credential: Any): String? = null
    override suspend fun signUpWithEmailAndPassword(email: String, password: String): Result<Unit> = Result.success(Unit)
    override suspend fun fetchSignInMethodsForEmail(email: String): EmailExistResult {
        requestedEmail = email
        return emailExistResult
    }
    override suspend fun sendPasswordResetEmail(email: String): Boolean {
        requestedEmail = email
        return sendResetResult
    }
    override suspend fun saveSignUpInformation(userInstance: UserInstance): Boolean = true
}

class CheckIfEmailExistsUseCaseTest {
    @Test
    fun `invoke delegates to repository and returns its result`() = runTest {
        val repo = FakeAuthenticationRepository().apply { emailExistResult = EmailExistResult(true, "EXISTS") }
        val useCase = CheckIfEmailExistsUseCase(repo)

        val result = useCase.invoke("a@b.com")

        assertEquals(EmailExistResult(true, "EXISTS"), result)
        assertEquals("a@b.com", repo.requestedEmail)
    }
}

class SendEmailResetPasswordUseCaseTest {
    @Test
    fun `invoke delegates to repository and returns its result`() = runTest {
        val repo = FakeAuthenticationRepository().apply { sendResetResult = true }
        val useCase = SendEmailResetPasswordUseCase(repo)

        val result = useCase.invoke("a@b.com")

        assertEquals(true, result)
        assertEquals("a@b.com", repo.requestedEmail)
    }
}
