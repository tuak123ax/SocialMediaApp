package com.minhtu.firesocialmedia

import com.minhtu.firesocialmedia.domain.entity.news.NewsInstance
import com.minhtu.firesocialmedia.domain.entity.notification.NotificationInstance
import com.minhtu.firesocialmedia.domain.entity.user.UserInstance
import com.minhtu.firesocialmedia.domain.repository.AuthenticationRepository
import com.minhtu.firesocialmedia.domain.repository.LocalRepository
import com.minhtu.firesocialmedia.domain.repository.UserRepository
import com.minhtu.firesocialmedia.domain.usecases.common.GetCurrentUserUidUseCase
import com.minhtu.firesocialmedia.domain.usecases.common.GetFCMTokenUseCase
import com.minhtu.firesocialmedia.domain.usecases.information.SaveSignUpInformationUseCase
import com.minhtu.firesocialmedia.presentation.information.InformationViewModel
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

class InfoFakeAuthRepository : AuthenticationRepository {
    var saveResult = true
    override suspend fun signInWithEmailAndPassword(email: String, password: String) = null
    override suspend fun saveAccountToLocalStorage(email: String, password: String) {}
    override suspend fun checkUserExists(email: String) = com.minhtu.firesocialmedia.domain.entity.signin.SignInState(false, null)
    override suspend fun checkLocalAccount() = null
    override suspend fun handleSignInGoogleResult(credential: Any) = null
    override suspend fun signUpWithEmailAndPassword(email: String, password: String) = Result.success(Unit)
    override suspend fun fetchSignInMethodsForEmail(email: String) = com.minhtu.firesocialmedia.domain.entity.forgotpassword.EmailExistResult(false, "")
    override suspend fun sendPasswordResetEmail(email: String) = true
    override suspend fun clearAccount() {}
    override suspend fun saveSignUpInformation(userInstance: UserInstance) = saveResult
}

class InfoFakeUserRepository(private val uid: String?) : UserRepository {
    override suspend fun getUser(userId: String, isCurrentUser: Boolean) = null
    override suspend fun getCurrentUserUid() = uid
    override suspend fun updateFCMTokenForCurrentUser(user: UserInstance) {}
    override suspend fun searchUserByName(name: String) = null
}

class InfoFakeLocalRepository(private val token: String) : LocalRepository {
    override suspend fun getFCMToken() = token
    override suspend fun storeUserFriendsToRoom(friends: List<UserInstance?>) {}
    override suspend fun storeNewsToRoom(news: List<NewsInstance>) {}
    override suspend fun storeNotificationsToRoom(notifications: List<NotificationInstance>) {}
    override suspend fun getUserFromRoom(userId: String) = null
    override suspend fun saveCurrentUserInfo(user: UserInstance) {}
}

@OptIn(ExperimentalCoroutinesApi::class)
class InformationViewModelTest {
    private val testDispatcher = StandardTestDispatcher()
    private lateinit var authRepo: InfoFakeAuthRepository
    private lateinit var userRepo: InfoFakeUserRepository
    private lateinit var localRepo: InfoFakeLocalRepository

    @BeforeTest
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        authRepo = InfoFakeAuthRepository()
        userRepo = InfoFakeUserRepository("testUid")
        localRepo = InfoFakeLocalRepository("testToken")
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun vm(): InformationViewModel = InformationViewModel(
        SaveSignUpInformationUseCase(authRepo),
        GetCurrentUserUidUseCase(userRepo),
        GetFCMTokenUseCase(localRepo),
        testDispatcher
    )

    @Test
    fun `finish sign up with username blank`() = runTest(testDispatcher) {
        val informationViewModel = vm()
        informationViewModel.updateUsername("")
        informationViewModel.finishSignUpStage()
        advanceUntilIdle()

        val status = informationViewModel.addInformationStatus.value
        assertEquals(false, status)
    }

    @Test
    fun `finish sign up with error from server`() = runTest(testDispatcher) {
        authRepo.saveResult = false
        val informationViewModel = vm()
        informationViewModel.updateEmail("email")
        informationViewModel.updateAvatar("avatar")
        informationViewModel.updateUsername("username")

        informationViewModel.finishSignUpStage()
        advanceUntilIdle()

        val status = informationViewModel.addInformationStatus.value
        assertEquals(false, status)
    }

    @Test
    fun `finish sign up success`() = runTest(testDispatcher) {
        authRepo.saveResult = true
        val informationViewModel = vm()
        informationViewModel.updateEmail("email")
        informationViewModel.updateAvatar("avatar")
        informationViewModel.updateUsername("username")

        informationViewModel.finishSignUpStage()
        advanceUntilIdle()

        val status = informationViewModel.addInformationStatus.value
        assertEquals(true, status)
    }
}