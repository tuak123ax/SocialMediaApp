package com.minhtu.firesocialmedia.domain.usecases.information

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
    var saveResult = true
    var savedUser: UserInstance? = null

    override suspend fun clearAccount() {}
    override suspend fun signInWithEmailAndPassword(email: String, password: String): SignInError? = null
    override suspend fun saveAccountToLocalStorage(email: String, password: String) {}
    override suspend fun checkUserExists(email: String): SignInState = SignInState(false, null)
    override suspend fun checkLocalAccount(): Credentials? = null
    override suspend fun handleSignInGoogleResult(credential: Any): String? = null
    override suspend fun signUpWithEmailAndPassword(email: String, password: String): Result<Unit> = Result.success(Unit)
    override suspend fun fetchSignInMethodsForEmail(email: String): EmailExistResult = EmailExistResult(false, "")
    override suspend fun sendPasswordResetEmail(email: String): Boolean = true
    override suspend fun saveSignUpInformation(userInstance: UserInstance): Boolean {
        savedUser = userInstance
        return saveResult
    }
}

class SaveSignUpInformationUseCaseTest {
    @Test
    fun `invoke delegates to repository and returns success result`() = runTest {
        val repo = FakeAuthenticationRepository().apply { saveResult = true }
        val useCase = SaveSignUpInformationUseCase(repo)
        val user = UserInstance(uid = "u1", name = "Alice")

        val result = useCase.invoke(user)

        assertEquals(true, result)
        assertEquals(user, repo.savedUser)
    }

    @Test
    fun `invoke returns false when repository save fails`() = runTest {
        val repo = FakeAuthenticationRepository().apply { saveResult = false }
        val useCase = SaveSignUpInformationUseCase(repo)

        val result = useCase.invoke(UserInstance(uid = "u1"))

        assertEquals(false, result)
    }
}
