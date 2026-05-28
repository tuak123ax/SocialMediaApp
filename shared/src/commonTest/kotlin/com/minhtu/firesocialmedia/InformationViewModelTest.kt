package com.minhtu.firesocialmedia

import com.minhtu.firesocialmedia.core.domain.entity.base.BaseNewsInstance
import com.minhtu.firesocialmedia.core.domain.entity.comment.CommentInstance
import com.minhtu.firesocialmedia.core.domain.entity.news.NewsInstance
import com.minhtu.firesocialmedia.core.domain.entity.notification.NotificationInstance
import com.minhtu.firesocialmedia.core.domain.entity.user.UserInstance
import com.minhtu.firesocialmedia.core.domain.repository.AuthenticationRepository
import com.minhtu.firesocialmedia.core.domain.repository.CommonDbRepository
import com.minhtu.firesocialmedia.core.domain.repository.LocalRepository
import com.minhtu.firesocialmedia.core.domain.repository.UserRepository
import com.minhtu.firesocialmedia.core.domain.usecases.common.GetCurrentUserUidUseCase
import com.minhtu.firesocialmedia.core.domain.usecases.common.GetFCMTokenUseCase
import com.minhtu.firesocialmedia.core.domain.usecases.information.SaveSignUpInformationUseCase
import com.minhtu.firesocialmedia.core.domain.usecases.signin.SaveLoginActivityInfoUseCase
import com.minhtu.firesocialmedia.presentation.information.InformationViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

class InfoFakeAuthRepository : AuthenticationRepository {
    var saveResult = true
    override suspend fun signInWithEmailAndPassword(email: String, password: String) = null
    override suspend fun saveAccountToLocalStorage(email: String, password: String) {}
    override suspend fun checkUserExists(email: String) = com.minhtu.firesocialmedia.core.domain.entity.signin.SignInState(false, null)
    override suspend fun checkLocalAccount() = null
    override suspend fun handleSignInGoogleResult(credential: Any) = null
    override suspend fun signUpWithEmailAndPassword(email: String, password: String) = Result.success(Unit)
    override suspend fun fetchSignInMethodsForEmail(email: String) = com.minhtu.firesocialmedia.core.domain.entity.forgotpassword.EmailExistResult(false, "")
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

class InfoFakeCommonDbRepository : CommonDbRepository {
    override suspend fun saveLikedPost(id: String, value: HashMap<String, Int>) = true
    override suspend fun saveNewToDatabase(instance: com.minhtu.firesocialmedia.core.domain.entity.news.NewsInstance) = true
    override suspend fun saveCommentToDatabase(selectedNewId: String, commentId: String, instance: CommentInstance) = true
    override suspend fun saveSubCommentToDatabase(id: String, selectedNewId: String, parentCommentId: String, instance: BaseNewsInstance) = true
    override suspend fun deleteCommentFromDatabase(selectedNewId: String, instance: BaseNewsInstance) {}
    override suspend fun deleteSubCommentFromDatabase(selectedNewId: String, parentCommentId: String, instance: BaseNewsInstance) {}
    override suspend fun updateCommentCountForNewInDatabase(id: String, value: Int) {}
    override suspend fun updateReplyCountForCommentInDatabase(id: String, currentCommentId: String, value: Int) {}
    override suspend fun updateLikeCountForNewInDatabase(id: String, value: Int) {}
    override suspend fun updateLikeCountForCommentInDatabase(selectedNewId: String, likedComment: String, value: Int) {}
    override suspend fun updateLikeCountForSubCommentInDatabase(selectedNewId: String, likedComment: String, parentCommentId: String, value: Int) {}
    override suspend fun saveLikedComments(id: String, map: HashMap<String, Int>) = true
    override suspend fun saveFriend(id: String, value: ArrayList<String>) {}
    override suspend fun saveFriendRequest(id: String, value: ArrayList<String>) {}
    override suspend fun syncData(currentUserId: String) = true
    override suspend fun clearLikedPosts() {}
    override suspend fun clearComments() {}
    override suspend fun loadNewsPostedWhenOffline() = emptyList<com.minhtu.firesocialmedia.core.domain.entity.news.NewsInstance>()
    override suspend fun deleteAllDraftPosts() = true
    override suspend fun deleteDraftPost(newId: String) = true
    override suspend fun clearLocalFriends() {}
    override suspend fun saveLoginActivityInfo(userId: String) {}
}

@OptIn(ExperimentalCoroutinesApi::class)
class InformationViewModelTest {
    private val testDispatcher = StandardTestDispatcher()
    private lateinit var authRepo: InfoFakeAuthRepository
    private lateinit var userRepo: InfoFakeUserRepository
    private lateinit var localRepo: InfoFakeLocalRepository
    private lateinit var commonDbRepo: InfoFakeCommonDbRepository

    @BeforeTest
    fun setup() {
        authRepo = InfoFakeAuthRepository()
        userRepo = InfoFakeUserRepository("testUid")
        localRepo = InfoFakeLocalRepository("testToken")
        commonDbRepo = InfoFakeCommonDbRepository()
    }

    private fun vm(dispatcher: CoroutineDispatcher): InformationViewModel = InformationViewModel(
        SaveSignUpInformationUseCase(authRepo),
        GetCurrentUserUidUseCase(userRepo),
        GetFCMTokenUseCase(localRepo),
        SaveLoginActivityInfoUseCase(commonDbRepo),
        dispatcher
    )

    @Test
    fun `finish sign up with username blank`() = runTest {
        val dispatcher = StandardTestDispatcher(testScheduler)
        val informationViewModel = vm(dispatcher)
        informationViewModel.updateUsername("")
        informationViewModel.finishSignUpStage()
        advanceUntilIdle()

        val status = informationViewModel.addInformationStatus.value
        assertEquals(false, status)
    }

    @Test
    fun `finish sign up with error from server`() = runTest {
        val dispatcher = StandardTestDispatcher(testScheduler)
        authRepo.saveResult = false
        val informationViewModel = vm(dispatcher)
        informationViewModel.updateEmail("email")
        informationViewModel.updateAvatar("avatar")
        informationViewModel.updateUsername("username")

        informationViewModel.finishSignUpStage()
        advanceUntilIdle()

        val status = informationViewModel.addInformationStatus.value
        assertEquals(false, status)
    }

    @Test
    fun `finish sign up success`() = runTest {
        val dispatcher = StandardTestDispatcher(testScheduler)
        authRepo.saveResult = true
        val informationViewModel = vm(dispatcher)
        informationViewModel.updateEmail("email")
        informationViewModel.updateAvatar("avatar")
        informationViewModel.updateUsername("username")

        informationViewModel.finishSignUpStage()
        advanceUntilIdle()

        val status = informationViewModel.addInformationStatus.value
        assertEquals(true, status)
    }

    @Test
    fun `saveLoginActivityInfo with null userId is a no-op`() = runTest {
        val dispatcher = StandardTestDispatcher(testScheduler)
        val viewModel = InformationViewModel(
            SaveSignUpInformationUseCase(authRepo),
            GetCurrentUserUidUseCase(InfoFakeUserRepository(null)),  // null uid
            GetFCMTokenUseCase(localRepo),
            SaveLoginActivityInfoUseCase(commonDbRepo),
            dispatcher
        )
        // Should complete without error, currentUserId.value remains null
        viewModel.saveLoginActivityInfo()
        advanceUntilIdle()
        // No crash and addInformationStatus remains null (never triggered)
        assertEquals(null, viewModel.addInformationStatus.value)
    }

    @Test
    fun `saveLoginActivityInfo with valid userId runs successfully`() = runTest {
        val dispatcher = StandardTestDispatcher(testScheduler)
        val viewModel = vm(dispatcher)
        // Run finishSignUpStage first to populate currentUserId inside the ViewModel
        viewModel.updateEmail("e")
        viewModel.updateAvatar("a")
        viewModel.updateUsername("username")
        viewModel.finishSignUpStage()
        advanceUntilIdle()

        // Now saveLoginActivityInfo should run without error
        viewModel.saveLoginActivityInfo()
        advanceUntilIdle()
        // No exception and status is still the result of finishSignUpStage
        assertEquals(true, viewModel.addInformationStatus.value)
    }
}