package com.minhtu.firesocialmedia.domain.usecases.signin

import com.minhtu.firesocialmedia.domain.entity.crypto.Credentials
import com.minhtu.firesocialmedia.domain.entity.forgotpassword.EmailExistResult
import com.minhtu.firesocialmedia.domain.entity.signin.SignInState
import com.minhtu.firesocialmedia.domain.entity.user.auth.UserInstance
import com.minhtu.firesocialmedia.domain.error.signin.SignInError
import com.minhtu.firesocialmedia.domain.repository.AuthenticationRepository
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

private class FakeAuthenticationRepository : AuthenticationRepository {
    var signInError: SignInError? = null
    var checkUserExistsResult: SignInState = SignInState(false, null)
    var localCredentials: Credentials? = null
    var googleResult: String? = null
    var rememberInvokedWith: Pair<String, String>? = null
    var signInInvokedWith: Pair<String, String>? = null

    override suspend fun clearAccount() {}
    override suspend fun signInWithEmailAndPassword(email: String, password: String): SignInError? {
        signInInvokedWith = email to password
        return signInError
    }
    override suspend fun saveAccountToLocalStorage(email: String, password: String) {
        rememberInvokedWith = email to password
    }
    override suspend fun checkUserExists(email: String): SignInState = checkUserExistsResult
    override suspend fun checkLocalAccount(): Credentials? = localCredentials
    override suspend fun handleSignInGoogleResult(credential: Any): String? = googleResult
    override suspend fun signUpWithEmailAndPassword(email: String, password: String): Result<Unit> = Result.success(Unit)
    override suspend fun fetchSignInMethodsForEmail(email: String): EmailExistResult = EmailExistResult(false, "")
    override suspend fun sendPasswordResetEmail(email: String): Boolean = true
    override suspend fun saveSignUpInformation(userInstance: UserInstance): Boolean = true
}

class SignInUseCaseTest {
    @Test
    fun `invoke returns null when repository sign in succeeds`() = runTest {
        val repo = FakeAuthenticationRepository()
        val useCase = SignInUseCase(repo)

        val result = useCase.invoke("a@b.com", "pw")

        assertNull(result)
        assertEquals("a@b.com" to "pw", repo.signInInvokedWith)
    }

    @Test
    fun `invoke returns the error from the repository on failure`() = runTest {
        val repo = FakeAuthenticationRepository().apply { signInError = SignInError.WrongPassword }
        val useCase = SignInUseCase(repo)

        val result = useCase.invoke("a@b.com", "wrong")

        assertEquals(SignInError.WrongPassword, result)
    }
}

class RememberPasswordUseCaseTest {
    @Test
    fun `invoke delegates credentials to repository`() = runTest {
        val repo = FakeAuthenticationRepository()
        val useCase = RememberPasswordUseCase(repo)

        useCase.invoke("a@b.com", "pw")

        assertEquals("a@b.com" to "pw", repo.rememberInvokedWith)
    }
}

class CheckUserExistsUseCaseTest {
    @Test
    fun `invoke returns state from repository`() = runTest {
        val repo = FakeAuthenticationRepository().apply { checkUserExistsResult = SignInState(true, null) }
        val useCase = CheckUserExistsUseCase(repo)

        val result = useCase.invoke("a@b.com")

        assertEquals(SignInState(true, null), result)
    }
}

class CheckLocalAccountUseCaseTest {
    @Test
    fun `invoke returns stored credentials`() = runTest {
        val repo = FakeAuthenticationRepository().apply { localCredentials = Credentials("a@b.com", "pw") }
        val useCase = CheckLocalAccountUseCase(repo)

        assertEquals(Credentials("a@b.com", "pw"), useCase.invoke())
    }

    @Test
    fun `invoke returns null when no credentials stored`() = runTest {
        val repo = FakeAuthenticationRepository().apply { localCredentials = null }
        val useCase = CheckLocalAccountUseCase(repo)

        assertNull(useCase.invoke())
    }
}

class HandleSignInGoogleResultUseCaseTest {
    @Test
    fun `invoke returns the resolved email from repository`() = runTest {
        val repo = FakeAuthenticationRepository().apply { googleResult = "a@b.com" }
        val useCase = HandleSignInGoogleResultUseCase(repo)

        assertEquals("a@b.com", useCase.invoke("credential"))
    }

    @Test
    fun `invoke returns null when repository cannot resolve credential`() = runTest {
        val repo = FakeAuthenticationRepository().apply { googleResult = null }
        val useCase = HandleSignInGoogleResultUseCase(repo)

        assertNull(useCase.invoke("bad-credential"))
    }
}
