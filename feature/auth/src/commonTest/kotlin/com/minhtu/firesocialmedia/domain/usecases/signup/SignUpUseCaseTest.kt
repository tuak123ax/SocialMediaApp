package com.minhtu.firesocialmedia.domain.usecases.signup

import com.minhtu.firesocialmedia.domain.entity.crypto.Credentials
import com.minhtu.firesocialmedia.domain.entity.forgotpassword.EmailExistResult
import com.minhtu.firesocialmedia.domain.entity.signin.SignInState
import com.minhtu.firesocialmedia.domain.entity.user.auth.UserInstance
import com.minhtu.firesocialmedia.domain.error.signin.SignInError
import com.minhtu.firesocialmedia.domain.repository.AuthenticationRepository
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

private class FakeAuthenticationRepository : AuthenticationRepository {
    var signUpResult: Result<Unit> = Result.success(Unit)
    var requestedCredentials: Pair<String, String>? = null

    override suspend fun clearAccount() {}
    override suspend fun signInWithEmailAndPassword(email: String, password: String): SignInError? = null
    override suspend fun saveAccountToLocalStorage(email: String, password: String) {}
    override suspend fun checkUserExists(email: String): SignInState = SignInState(false, null)
    override suspend fun checkLocalAccount(): Credentials? = null
    override suspend fun handleSignInGoogleResult(credential: Any): String? = null
    override suspend fun signUpWithEmailAndPassword(email: String, password: String): Result<Unit> {
        requestedCredentials = email to password
        return signUpResult
    }
    override suspend fun fetchSignInMethodsForEmail(email: String): EmailExistResult = EmailExistResult(false, "")
    override suspend fun sendPasswordResetEmail(email: String): Boolean = true
    override suspend fun saveSignUpInformation(userInstance: UserInstance): Boolean = true
}

class SignUpUseCaseTest {
    @Test
    fun `invoke delegates credentials and returns success result`() = runTest {
        val repo = FakeAuthenticationRepository()
        val useCase = SignUpUseCase(repo)

        val result = useCase.invoke("a@b.com", "pw")

        assertTrue(result.isSuccess)
        assertEquals("a@b.com" to "pw", repo.requestedCredentials)
    }

    @Test
    fun `invoke returns failure result from repository`() = runTest {
        val repo = FakeAuthenticationRepository().apply { signUpResult = Result.failure(Exception("boom")) }
        val useCase = SignUpUseCase(repo)

        val result = useCase.invoke("a@b.com", "pw")

        assertTrue(result.isFailure)
        assertEquals("boom", result.exceptionOrNull()?.message)
    }
}
