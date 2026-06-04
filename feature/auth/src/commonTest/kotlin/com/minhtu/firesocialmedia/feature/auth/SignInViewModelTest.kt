package com.minhtu.firesocialmedia.feature.auth

import com.minhtu.firesocialmedia.core.domain.entity.base.BaseNewsInstance
import com.minhtu.firesocialmedia.core.domain.entity.comment.CommentInstance
import com.minhtu.firesocialmedia.core.domain.entity.crypto.Credentials
import com.minhtu.firesocialmedia.core.domain.entity.forgotpassword.EmailExistResult
import com.minhtu.firesocialmedia.core.domain.entity.news.NewsInstance
import com.minhtu.firesocialmedia.core.domain.entity.signin.SignInState
import com.minhtu.firesocialmedia.core.domain.entity.user.UserInstance
import com.minhtu.firesocialmedia.core.domain.error.signin.SignInError
import com.minhtu.firesocialmedia.core.domain.repository.AuthenticationRepository
import com.minhtu.firesocialmedia.core.domain.repository.CommonDbRepository
import com.minhtu.firesocialmedia.core.domain.repository.UserRepository
import com.minhtu.firesocialmedia.core.domain.usecases.common.GetCurrentUserUidUseCase
import com.minhtu.firesocialmedia.core.domain.usecases.common.GetUserUseCase
import com.minhtu.firesocialmedia.core.domain.usecases.signin.CheckLocalAccountUseCase
import com.minhtu.firesocialmedia.core.domain.usecases.signin.CheckUserExistsUseCase
import com.minhtu.firesocialmedia.core.domain.usecases.signin.HandleSignInGoogleResultUseCase
import com.minhtu.firesocialmedia.core.domain.usecases.signin.RememberPasswordUseCase
import com.minhtu.firesocialmedia.core.domain.usecases.signin.SaveLoginActivityInfoUseCase
import com.minhtu.firesocialmedia.core.domain.usecases.signin.SignInUseCase
import com.minhtu.firesocialmedia.feature.auth.presentation.signin.SignInViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

private class SignInFakeAuthRepository : AuthenticationRepository {
    var signInError: SignInError? = null
    var signInInvokedWithEmail: String? = null
    var rememberInvokedWith: Pair<String, String>? = null
    var checkUserExistsResult: SignInState = SignInState(true, null)
    var localCredentials: Credentials? = null

    override suspend fun signInWithEmailAndPassword(email: String, password: String): SignInError? {
        signInInvokedWithEmail = email
        return signInError
    }
    override suspend fun saveAccountToLocalStorage(email: String, password: String) {
        rememberInvokedWith = email to password
    }
    override suspend fun checkUserExists(email: String): SignInState = checkUserExistsResult
    override suspend fun checkLocalAccount(): Credentials? = localCredentials
    override suspend fun handleSignInGoogleResult(credential: Any): String? = null
    override suspend fun signUpWithEmailAndPassword(email: String, password: String): Result<Unit> = Result.success(Unit)
    override suspend fun fetchSignInMethodsForEmail(email: String): EmailExistResult = EmailExistResult(false, "")
    override suspend fun sendPasswordResetEmail(email: String): Boolean = true
    override suspend fun clearAccount() {}
    override suspend fun saveSignUpInformation(userInstance: UserInstance): Boolean = true
}

private class SignInFakeUserRepository(
    private val currentUserUid: String? = null,
    private val user: UserInstance? = null
) : UserRepository {
    override suspend fun getUser(userId: String, isCurrentUser: Boolean): UserInstance? = user
    override suspend fun getCurrentUserUid(): String? = currentUserUid
    override suspend fun updateFCMTokenForCurrentUser(user: UserInstance) {}
    override suspend fun searchUserByName(name: String): List<UserInstance>? = null
}

private class SignInFakeCommonDbRepository : CommonDbRepository {
    var savedLoginActivityForUserId: String? = null
    override suspend fun saveLikedPost(id: String, value: HashMap<String, Int>): Boolean = true
    override suspend fun saveNewToDatabase(instance: NewsInstance): Boolean = true
    override suspend fun saveCommentToDatabase(selectedNewId: String, commentId: String, instance: CommentInstance): Boolean = true
    override suspend fun saveSubCommentToDatabase(id: String, selectedNewId: String, parentCommentId: String, instance: BaseNewsInstance): Boolean = true
    override suspend fun deleteCommentFromDatabase(selectedNewId: String, instance: BaseNewsInstance) {}
    override suspend fun deleteSubCommentFromDatabase(selectedNewId: String, parentCommentId: String, instance: BaseNewsInstance) {}
    override suspend fun updateCommentCountForNewInDatabase(id: String, value: Int) {}
    override suspend fun updateReplyCountForCommentInDatabase(id: String, currentCommentId: String, value: Int) {}
    override suspend fun updateLikeCountForNewInDatabase(id: String, value: Int) {}
    override suspend fun updateLikeCountForCommentInDatabase(selectedNewId: String, likedComment: String, value: Int) {}
    override suspend fun updateLikeCountForSubCommentInDatabase(selectedNewId: String, likedComment: String, parentCommentId: String, value: Int) {}
    override suspend fun saveLikedComments(id: String, map: HashMap<String, Int>): Boolean = true
    override suspend fun saveFriend(id: String, value: ArrayList<String>) {}
    override suspend fun saveFriendRequest(id: String, value: ArrayList<String>) {}
    override suspend fun syncData(currentUserId: String): Boolean = true
    override suspend fun clearLikedPosts() {}
    override suspend fun clearComments() {}
    override suspend fun loadNewsPostedWhenOffline(): List<NewsInstance> = emptyList()
    override suspend fun deleteAllDraftPosts(): Boolean = true
    override suspend fun deleteDraftPost(newId: String): Boolean = true
    override suspend fun clearLocalFriends() {}
    override suspend fun saveLoginActivityInfo(userId: String) { savedLoginActivityForUserId = userId }
}

@OptIn(ExperimentalCoroutinesApi::class)
class SignInViewModelTest {

    private fun buildViewModel(
        scheduler: kotlinx.coroutines.test.TestCoroutineScheduler,
        authRepo: SignInFakeAuthRepository = SignInFakeAuthRepository(),
        userRepo: SignInFakeUserRepository = SignInFakeUserRepository(),
        commonDb: SignInFakeCommonDbRepository = SignInFakeCommonDbRepository()
    ): Pair<SignInViewModel, Triple<SignInFakeAuthRepository, SignInFakeUserRepository, SignInFakeCommonDbRepository>> {
        val vm = SignInViewModel(
            SignInUseCase(authRepo),
            RememberPasswordUseCase(authRepo),
            CheckUserExistsUseCase(authRepo),
            CheckLocalAccountUseCase(authRepo),
            HandleSignInGoogleResultUseCase(authRepo),
            GetCurrentUserUidUseCase(userRepo),
            GetUserUseCase(userRepo),
            SaveLoginActivityInfoUseCase(commonDb),
            StandardTestDispatcher(scheduler)
        )
        return vm to Triple(authRepo, userRepo, commonDb)
    }

    @Test
    fun `signIn with empty email and password emits DataEmpty error`() = runTest {
        val (vm, _) = buildViewModel(testScheduler)
        vm.signIn { }
        advanceUntilIdle()

        val state = vm.signInState.value
        assertEquals(false, state.isAuthenticated)
        assertEquals(SignInError.DataEmpty, state.error)
    }

    @Test
    fun `signIn success delegates to checkUserExists and emits authenticated state`() = runTest {
        val authRepo = SignInFakeAuthRepository().apply {
            checkUserExistsResult = SignInState(true, null)
        }
        val (vm, _) = buildViewModel(testScheduler, authRepo = authRepo)
        vm.updateEmail("user@example.com")
        vm.updatePassword("123456")

        vm.signIn { }
        advanceUntilIdle()

        val state = vm.signInState.value
        assertEquals(true, state.isAuthenticated)
        assertNull(state.error)
    }

    @Test
    fun `signIn failure surfaces use case error in signInState`() = runTest {
        val authRepo = SignInFakeAuthRepository().apply { signInError = SignInError.WrongPassword }
        val (vm, _) = buildViewModel(testScheduler, authRepo = authRepo)
        vm.updateEmail("user@example.com")
        vm.updatePassword("wrongPassword")

        vm.signIn { }
        advanceUntilIdle()

        val state = vm.signInState.value
        assertEquals(false, state.isAuthenticated)
        assertEquals(SignInError.WrongPassword, state.error)
    }

    @Test
    fun `signIn with rememberPassword on triggers remember use case`() = runTest {
        val authRepo = SignInFakeAuthRepository()
        val (vm, fakes) = buildViewModel(testScheduler, authRepo = authRepo)
        vm.updateEmail("user@example.com")
        vm.updatePassword("123456")
        vm.updateRememberPassword(true)

        vm.signIn { }
        advanceUntilIdle()

        val invoked = fakes.first.rememberInvokedWith
        assertNotNull(invoked)
        assertEquals("user@example.com", invoked.first)
        assertEquals("123456", invoked.second)
    }

    @Test
    fun `signIn lowercases the email before invoking use case`() = runTest {
        val authRepo = SignInFakeAuthRepository()
        val (vm, fakes) = buildViewModel(testScheduler, authRepo = authRepo)
        vm.updateEmail("USER@EXAMPLE.COM")
        vm.updatePassword("123456")

        vm.signIn { }
        advanceUntilIdle()

        assertEquals("user@example.com", fakes.first.signInInvokedWithEmail)
    }

    @Test
    fun `resetSignInStatus clears signInState`() = runTest {
        val (vm, _) = buildViewModel(testScheduler)
        vm.signIn { } // triggers DataEmpty
        advanceUntilIdle()
        assertEquals(SignInError.DataEmpty, vm.signInState.value.error)

        vm.resetSignInStatus()

        assertEquals(false, vm.signInState.value.isAuthenticated)
        assertNull(vm.signInState.value.error)
    }

    @Test
    fun `checkLocalAccount populates email and password when credentials exist`() = runTest {
        val authRepo = SignInFakeAuthRepository().apply { localCredentials = Credentials("a@b.com", "pw") }
        val (vm, _) = buildViewModel(testScheduler, authRepo = authRepo)
        vm.checkLocalAccount()
        advanceUntilIdle()

        assertEquals("a@b.com", vm.email.value)
        assertEquals("pw", vm.password.value)
        assertNotNull(vm.localCredentials.value)
    }

    @Test
    fun `check2FAStatus updates state from fetched user`() = runTest {
        val user = UserInstance(uid = "u1", name = "Alice", email = "a@b.com").apply { twoFAEnabled = true }
        val userRepo = SignInFakeUserRepository(currentUserUid = "u1", user = user)
        val (vm, fakes) = buildViewModel(testScheduler, userRepo = userRepo)
        vm.check2FAStatus()
        advanceUntilIdle()

        assertEquals(true, vm.check2FAStatus.value)
        assertEquals("u1", vm.currentUser.value?.uid)
        assertEquals("u1", fakes.third.savedLoginActivityForUserId)
    }

    @Test
    fun `reset clears all in-memory fields`() = runTest {
        val (vm, _) = buildViewModel(testScheduler)
        vm.updateEmail("a@b.com")
        vm.updatePassword("123")
        vm.updateRememberPassword(true)

        vm.reset()

        assertEquals("", vm.email.value)
        assertEquals("", vm.password.value)
        assertEquals(false, vm.rememberPassword.value)
        assertNull(vm.currentUser.value)
    }
}
