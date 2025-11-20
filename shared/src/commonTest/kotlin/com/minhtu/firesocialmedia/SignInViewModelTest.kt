package com.minhtu.firesocialmedia

import com.minhtu.firesocialmedia.data.remote.dto.crypto.CredentialsDTO
import com.minhtu.firesocialmedia.data.remote.dto.signin.SignInDTO
import com.minhtu.firesocialmedia.data.remote.service.auth.AuthService
import com.minhtu.firesocialmedia.data.local.service.crypto.CryptoService
import com.minhtu.firesocialmedia.data.remote.service.database.DatabaseService
import com.minhtu.firesocialmedia.data.remote.service.signinlauncher.SignInLauncher
import com.minhtu.firesocialmedia.data.remote.service.clipboard.ClipboardService
import com.minhtu.firesocialmedia.data.remote.service.call.AudioCallService
import com.minhtu.firesocialmedia.data.local.service.room.RoomService
import com.minhtu.firesocialmedia.data.remote.service.permission.PermissionManager
import com.minhtu.firesocialmedia.di.PlatformContext
import com.minhtu.firesocialmedia.domain.core.NetworkMonitor
import com.minhtu.firesocialmedia.domain.error.signin.SignInError
import com.minhtu.firesocialmedia.presentation.signin.SignInViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

@Suppress("UNCHECKED_CAST")
@OptIn(ExperimentalCoroutinesApi::class)
class SignInViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    // Simple fakes instead of mockative to avoid runtime NoSuchMethodError
    private class FakeAuthService : AuthService {
        var signInError: SignInError? = null
        var googleResult: String? = null
        override suspend fun signInWithEmailAndPassword(email: String, password: String): SignInError? = signInError
        override suspend fun signUpWithEmailAndPassword(email: String, password: String) = Result.success(Unit)
        override suspend fun getCurrentUserUid(): String? = null
        override suspend fun getCurrentUserEmail(): String? = null
        override suspend fun fetchSignInMethodsForEmail(email: String) = com.minhtu.firesocialmedia.domain.entity.forgotpassword.EmailExistResult(false, "")
        override suspend fun sendPasswordResetEmail(email: String) = true
        override suspend fun handleSignInGoogleResult(credentialsDTO: Any): String? = googleResult
    }

    private class FakeCryptoService : CryptoService {
        var saved: CredentialsDTO? = null
        var load: CredentialsDTO? = null
        override fun saveAccount(email: String, password: String) { saved = CredentialsDTO(email, password) }
        override suspend fun loadAccount(): CredentialsDTO? = load
        override suspend fun clearAccount() { saved = null; load = null }
        override suspend fun getFCMToken(): String = ""
        override suspend fun saveCurrentUserInfo(user: com.minhtu.firesocialmedia.data.remote.dto.user.UserDTO) {}
        override suspend fun getCurrentUserInfo(): com.minhtu.firesocialmedia.data.remote.dto.user.UserDTO? = null
    }

    private class FakeDatabaseService : DatabaseService {
        var userExists: Boolean = true
        override suspend fun updateFCMTokenForCurrentUser(currentUser: com.minhtu.firesocialmedia.data.remote.dto.user.UserDTO) {}
        override suspend fun checkUserExists(email: String): SignInDTO = SignInDTO(userExists, "")
        override suspend fun saveValueToDatabase(id: String, path: String, value: HashMap<String, Int>, externalPath: String) = true
        override suspend fun updateCountValueInDatabase(id: String, path: String, externalPath: String, value: Int) {}
        override suspend fun deleteNewsFromDatabase(path: String, new: com.minhtu.firesocialmedia.data.remote.dto.news.NewsDTO) {}
        override suspend fun deleteCommentFromDatabase(path: String, comment: com.minhtu.firesocialmedia.domain.entity.base.BaseNewsInstance) {}
        override suspend fun saveInstanceToDatabase(commentId: String, path: String, instance: com.minhtu.firesocialmedia.domain.entity.base.BaseNewsInstance) = true
        override suspend fun saveNewToDatabase(commentId: String, path: String, instance: com.minhtu.firesocialmedia.data.remote.dto.news.NewsDTO) = true
        override suspend fun getAllUsers(path: String) = null
        override suspend fun getUser(userId: String) = null
        override suspend fun getNew(newId: String) = null
        override suspend fun getLatestNews(number: Int, lastTimePosted: Double?, lastKey: String?, path: String) = com.minhtu.firesocialmedia.data.remote.dto.home.LatestNewsDTO(emptyList(), null, null)
        override suspend fun getAllComments(path: String, newsId: String) = null
        override suspend fun getAllNotificationsOfUser(path: String, currentUserUid: String) = null
        override suspend fun saveListToDatabase(id: String, path: String, value: ArrayList<String>, externalPath: String) {}
        override suspend fun downloadImage(image: String, fileName: String) = true
        override suspend fun updateNewsFromDatabase(path: String, newContent: String, newImage: String, newVideo: String, new: com.minhtu.firesocialmedia.data.remote.dto.news.NewsDTO) = true
        override suspend fun saveSignUpInformation(user: com.minhtu.firesocialmedia.data.remote.dto.user.UserDTO) = true
        override suspend fun saveNotificationToDatabase(id: String, path: String, instance: ArrayList<com.minhtu.firesocialmedia.data.remote.dto.notification.NotificationDTO>) {}
        override suspend fun deleteNotificationFromDatabase(id: String, path: String, notification: com.minhtu.firesocialmedia.data.remote.dto.notification.NotificationDTO) {}
        override suspend fun sendOfferToFireBase(sessionId: String, offer: com.minhtu.firesocialmedia.data.remote.dto.call.OfferAnswerDTO, sendOfferCallBack: com.minhtu.firesocialmedia.utils.Utils.Companion.BasicCallBack) {}
        override suspend fun sendIceCandidateToFireBase(sessionId: String, iceCandidate: com.minhtu.firesocialmedia.data.remote.dto.call.IceCandidateDTO, whichCandidate: String, sendIceCandidateCallBack: com.minhtu.firesocialmedia.utils.Utils.Companion.BasicCallBack) {}
        override suspend fun sendCallSessionToFirebase(session: com.minhtu.firesocialmedia.data.remote.dto.call.AudioCallSessionDTO, sendCallSessionCallBack: com.minhtu.firesocialmedia.utils.Utils.Companion.BasicCallBack) {}
        override suspend fun sendCallStatusToFirebase(sessionId: String, status: com.minhtu.firesocialmedia.domain.entity.call.CallStatus) = true
        override suspend fun deleteCallSession(sessionId: String) = true
        override suspend fun observePhoneCall(isInCall: MutableStateFlow<Boolean>, currentUserId: String, phoneCallCallBack: (com.minhtu.firesocialmedia.data.remote.dto.call.CallingRequestDTO) -> Unit, endCallSession: (Boolean) -> Unit, whoEndCallCallBack: (String) -> Unit, iceCandidateCallBack: (iceCandidates: Map<String, com.minhtu.firesocialmedia.data.remote.dto.call.IceCandidateDTO>?) -> Unit) {}
        override suspend fun observePhoneCallWithoutCheckingInCall(currentUserId: String, phoneCallCallBack: (com.minhtu.firesocialmedia.data.remote.dto.call.CallingRequestDTO) -> Unit, endCallSession: (Boolean) -> Unit, whoEndCallCallBack: (String) -> Unit, iceCandidateCallBack: (iceCandidates: Map<String, com.minhtu.firesocialmedia.data.remote.dto.call.IceCandidateDTO>?) -> Unit) {}
        override suspend fun sendAnswerToFirebase(sessionId: String, answer: com.minhtu.firesocialmedia.data.remote.dto.call.OfferAnswerDTO, sendIceCandidateCallBack: com.minhtu.firesocialmedia.utils.Utils.Companion.BasicCallBack) {}
        override suspend fun updateAnswerInFirebase(sessionId: String, updateContent: String, updateField: String, updateAnswerCallBack: com.minhtu.firesocialmedia.utils.Utils.Companion.BasicCallBack) {}
        override suspend fun updateOfferInFirebase(sessionId: String, updateContent: String, updateField: String, updateOfferCallBack: com.minhtu.firesocialmedia.utils.Utils.Companion.BasicCallBack) {}
        override suspend fun isCalleeInActiveCall(calleeId: String, callPath: String): Boolean? = null
        override suspend fun observeAnswerFromCallee(sessionId: String, answerCallBack: (com.minhtu.firesocialmedia.data.remote.dto.call.OfferAnswerDTO) -> Unit, rejectCallBack: () -> Unit) {}
        override suspend fun observeCallStatus(sessionId: String, callStatusCallBack: com.minhtu.firesocialmedia.utils.Utils.Companion.CallStatusCallBack) {}
        override suspend fun cancelObserveAnswerFromCallee(sessionId: String, callPath: String) {}
        override suspend fun observeIceCandidatesFromCallee(sessionId: String, iceCandidateCallBack: (iceCandidate: com.minhtu.firesocialmedia.data.remote.dto.call.IceCandidateDTO) -> Unit) {}
        override suspend fun observeVideoCall(sessionId: String, videoCallCallBack: (offer: com.minhtu.firesocialmedia.data.remote.dto.call.OfferAnswerDTO) -> Unit) {}
        override suspend fun searchUserByName(name: String, path: String) = null
        override suspend fun sendWhoEndCall(sessionId: String, whoEndCall: String) = true
        override fun stopObservePhoneCall() {}
    }

    private class FakePlatformContext(
        override val auth: AuthService,
        override val crypto: CryptoService,
        override val database: DatabaseService
    ) : PlatformContext {
        override val clipboard: ClipboardService = object : ClipboardService { override fun copy(text: String) {} }
        override val audioCall: AudioCallService = object : AudioCallService {
            override suspend fun startCallService(sessionId: String, caller: com.minhtu.firesocialmedia.data.remote.dto.user.UserDTO, callee: com.minhtu.firesocialmedia.data.remote.dto.user.UserDTO) {}
            override suspend fun startVideoCall(onStartVideoCall: suspend (videoTrack: com.minhtu.firesocialmedia.platform.WebRTCVideoTrack) -> Unit) {}
            override suspend fun startVideoCallService(sessionId: String, caller: com.minhtu.firesocialmedia.data.remote.dto.user.UserDTO, callee: com.minhtu.firesocialmedia.data.remote.dto.user.UserDTO, currentUserId: String?, remoteVideoOffer: com.minhtu.firesocialmedia.data.remote.dto.call.OfferAnswerDTO?) {}
            override suspend fun initialize(onInitializeFinished: () -> Unit, onIceCandidateCreated: (iceCandidateData: com.minhtu.firesocialmedia.data.remote.dto.call.IceCandidateDTO) -> Unit, onRemoteVideoTrackReceived: (remoteVideoTrack: com.minhtu.firesocialmedia.platform.WebRTCVideoTrack) -> Unit) {}
            override suspend fun stopCall() {}
            override suspend fun acceptCallFromApp(sessionId: String, calleeId: String?) {}
            override suspend fun callerEndCallFromApp(currentUser: String) {}
            override suspend fun calleeEndCallFromApp(sessionId: String, currentUser: String) {}
            override suspend fun rejectVideoCall() {}
            override suspend fun createOffer(onOfferCreated: (offer: com.minhtu.firesocialmedia.data.remote.dto.call.OfferAnswerDTO) -> Unit) {}
            override suspend fun createVideoOffer(onOfferCreated: (offer: com.minhtu.firesocialmedia.data.remote.dto.call.OfferAnswerDTO) -> Unit) {}
            override suspend fun createAnswer(videoSupport: Boolean, onAnswerCreated: (answer: com.minhtu.firesocialmedia.data.remote.dto.call.OfferAnswerDTO) -> Unit) {}
            override suspend fun setRemoteDescription(remoteOfferAnswer: com.minhtu.firesocialmedia.data.remote.dto.call.OfferAnswerDTO) {}
            override suspend fun addIceCandidate(sdp: String, sdpMid: String, sdpMLineIndex: Int) {}
            override suspend fun setupAudioTrack() {}
            override suspend fun releaseResources() {}
        }
        override val room: RoomService = object : RoomService {
            override suspend fun storeUserFriendsToRoom(friends: List<com.minhtu.firesocialmedia.data.local.entity.UserEntity?>) {}
            override suspend fun storeUserFriendToRoom(friend: com.minhtu.firesocialmedia.data.local.entity.UserEntity) {}
            override suspend fun storeNewsToRoom(news: List<com.minhtu.firesocialmedia.data.local.entity.NewsEntity>) {}
            override suspend fun storeNotificationsToRoom(notifications: List<com.minhtu.firesocialmedia.data.local.entity.NotificationEntity>) {}
            override suspend fun getUserFromRoom(userId: String) = null
            override suspend fun getAllNotifications() = emptyList<com.minhtu.firesocialmedia.data.local.entity.NotificationEntity>()
            override suspend fun getFirstPage(number: Int) = emptyList<com.minhtu.firesocialmedia.data.local.entity.NewsEntity>()
            override suspend fun getPageAfter(number: Int, lastTimePosted: Long, lastKey: String?) = emptyList<com.minhtu.firesocialmedia.data.local.entity.NewsEntity>()
            override suspend fun getNewById(newId: String) = null
            override suspend fun saveLikedPost(value: List<com.minhtu.firesocialmedia.data.local.entity.LikedPostEntity>) {}
            override suspend fun getAllLikedPosts() = emptyList<com.minhtu.firesocialmedia.data.local.entity.LikedPostEntity>()
            override suspend fun clearLikedPosts() {}
            override suspend fun saveComment(commentEntity: com.minhtu.firesocialmedia.data.local.entity.CommentEntity) {}
            override suspend fun getAllComments() = emptyList<com.minhtu.firesocialmedia.data.local.entity.CommentEntity>()
            override suspend fun clearComments() {}
            override suspend fun hasLikedPost() = false
            override suspend fun hasComment() = false
            override suspend fun saveNews(new: com.minhtu.firesocialmedia.data.local.entity.NewsEntity) {}
            override suspend fun loadNewsPostedWhenOffline() = emptyList<com.minhtu.firesocialmedia.data.local.entity.NewsEntity>()
            override suspend fun deleteDraftPost(id: String) {}
            override suspend fun deleteAllDraftPosts() {}
        }
        override val permissionManager: PermissionManager = object : PermissionManager {
            override suspend fun requestCameraAndAudioPermissions() = true
            override suspend fun requestAudioPermission() = true
        }
        override val networkMonitor: NetworkMonitor = object : NetworkMonitor { override val isOnline = flowOf(true) }
    }

    private lateinit var authService: FakeAuthService
    private lateinit var cryptoService: FakeCryptoService
    private lateinit var databaseService: FakeDatabaseService
    private lateinit var platform: PlatformContext

    @BeforeTest
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        authService = FakeAuthService()
        cryptoService = FakeCryptoService()
        databaseService = FakeDatabaseService()
        platform = FakePlatformContext(authService, cryptoService, databaseService)
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    // Helper to create VM with test dispatcher, wiring use cases via AppModule
    private fun vm(): SignInViewModel {
        val repo = com.minhtu.firesocialmedia.di.AppModule.provideAuthenticationRepository(platform)
        val signInUseCase = com.minhtu.firesocialmedia.di.AppModule.provideSignInUseCase(repo)
        val rememberPasswordUseCase = com.minhtu.firesocialmedia.di.AppModule.provideRememberPasswordUseCase(repo)
        val checkUserExistsUseCase = com.minhtu.firesocialmedia.di.AppModule.provideCheckUserExistsUseCase(repo)
        val checkLocalAccountUseCase = com.minhtu.firesocialmedia.di.AppModule.provideCheckLocalAccountUseCase(repo)
        val handleSignInGoogleResultUseCase = com.minhtu.firesocialmedia.di.AppModule.provideHandleSignInGoogleResultUseCase(repo)
        return SignInViewModel(
            signInUseCase,
            rememberPasswordUseCase,
            checkUserExistsUseCase,
            checkLocalAccountUseCase,
            handleSignInGoogleResultUseCase,
            testDispatcher
        )
    }

    @Test
    fun `signIn with blank email shows error`() = runTest(testDispatcher) {
        val viewModel: SignInViewModel = vm()
        viewModel.resetSignInStatus()
        viewModel.updateEmail("")
        viewModel.updatePassword("somepassword")

        viewModel.signIn(showLoading = {})
        advanceUntilIdle()

        val signInState = viewModel.signInState.value
        assertEquals(false, signInState.signInStatus)
        assertEquals(SignInError.DataEmpty, signInState.error)
    }

    @Test
    fun `signIn with blank password shows error`() = runTest(testDispatcher) {
        val viewModel: SignInViewModel = vm()
        viewModel.resetSignInStatus()
        viewModel.updateEmail("test1234@gmail.com")
        viewModel.updatePassword("")

        viewModel.signIn(showLoading = {})
        advanceUntilIdle()

        val signInState = viewModel.signInState.value
        assertEquals(false, signInState.signInStatus)
        assertEquals(SignInError.DataEmpty, signInState.error)
    }

    @Test
    fun `signIn with both email and password are blank shows error`() = runTest(testDispatcher) {
        val viewModel: SignInViewModel = vm()
        viewModel.resetSignInStatus()
        viewModel.updateEmail("")
        viewModel.updatePassword("")

        viewModel.signIn(showLoading = {})
        advanceUntilIdle()

        val signInState = viewModel.signInState.value
        assertEquals(false, signInState.signInStatus)
        assertEquals(SignInError.DataEmpty, signInState.error)
    }

    @Test
    fun `signIn with valid email and password triggers success flow`() = runTest(testDispatcher) {
        val email = "test@gmail.com"
        val password = "securepassword"

        authService.signInError = null
        databaseService.userExists = true

        val viewModel: SignInViewModel = vm()
        viewModel.resetSignInStatus()
        viewModel.updateEmail(email.lowercase())
        viewModel.updatePassword(password)
        viewModel.updateRememberPassword(true)

        viewModel.signIn(showLoading = {})
        advanceUntilIdle()

        val signInState = viewModel.signInState.value
        assertEquals(true, signInState.signInStatus)
    }

    @Test
    fun `signIn with invalid email and password triggers fail flow`() = runTest(testDispatcher) {
        val email = "wrongTestAccount@gmail.com"
        val password = "securepassword"

        authService.signInError = SignInError.Unknown("Login failed")

        val viewModel: SignInViewModel = vm()
        viewModel.resetSignInStatus()
        viewModel.updateEmail(email.lowercase())
        viewModel.updatePassword(password)

        viewModel.signIn(showLoading = {})
        advanceUntilIdle()

        val signInState = viewModel.signInState.value
        assertEquals(false, signInState.signInStatus)
    }

    @Test
    fun `login with account that is in local storage triggers success flow`() = runTest(testDispatcher) {
        val correctCredentials = CredentialsDTO("correctuser@gmail.com", "123321")
        cryptoService.load = correctCredentials
        authService.signInError = null
        databaseService.userExists = true

        val viewModel: SignInViewModel = vm()

        viewModel.checkLocalAccount()
        advanceUntilIdle()
        viewModel.signIn(showLoading = {})
        advanceUntilIdle()

        val signInState = viewModel.signInState.value
        assertEquals(true, signInState.signInStatus)
    }

    @Test
    fun `login with Google triggers success flow`() = runTest(testDispatcher) {
        val viewModel: SignInViewModel = vm()
        viewModel.setSignInLauncher(object : SignInLauncher { override fun launchGoogleSignIn() {} })

        authService.googleResult = "correctemail@gmail.com"
        databaseService.userExists = true

        viewModel.signInWithGoogle()
        viewModel.handleSignInResult(Any())
        advanceUntilIdle()

        val signInState = viewModel.signInState.value
        assertEquals(true, signInState.signInStatus)
    }

    @Test
    fun `login with Google triggers fail flow`() = runTest(testDispatcher) {
        val viewModel: SignInViewModel = vm()
        viewModel.setSignInLauncher(object : SignInLauncher { override fun launchGoogleSignIn() {} })

        authService.googleResult = null

        viewModel.signInWithGoogle()
        viewModel.handleSignInResult(Any())
        advanceUntilIdle()

        val signInState = viewModel.signInState.value
        assertEquals(false, signInState.signInStatus)
    }
}

