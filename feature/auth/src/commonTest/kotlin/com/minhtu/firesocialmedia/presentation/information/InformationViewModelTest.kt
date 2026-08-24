package com.minhtu.firesocialmedia.presentation.information

import com.minhtu.firesocialmedia.data.remote.auth.dto.user.UserDTO
import com.minhtu.firesocialmedia.data.local.service.crypto.AuthCryptoService
import com.minhtu.firesocialmedia.data.remote.auth.dto.crypto.CredentialsDTO
import com.minhtu.firesocialmedia.data.remote.auth.dto.settings.auth.IpInfoResponseDTO
import com.minhtu.firesocialmedia.data.remote.auth.service.database.AuthDatabaseService
import com.minhtu.firesocialmedia.domain.entity.crypto.Credentials
import com.minhtu.firesocialmedia.domain.entity.forgotpassword.EmailExistResult
import com.minhtu.firesocialmedia.domain.entity.signin.SignInState
import com.minhtu.firesocialmedia.domain.entity.user.auth.UserInstance
import com.minhtu.firesocialmedia.domain.error.signin.SignInError
import com.minhtu.firesocialmedia.domain.repository.AuthenticationRepository
import com.minhtu.firesocialmedia.domain.repository.auth.UserRepository
import com.minhtu.firesocialmedia.domain.usecases.common.auth.GetCurrentUserUidUseCase
import com.minhtu.firesocialmedia.domain.usecases.common.auth.GetFCMTokenUseCase
import com.minhtu.firesocialmedia.domain.usecases.information.SaveSignUpInformationUseCase
import com.minhtu.firesocialmedia.domain.usecases.signin.SaveLoginActivityInfoUseCase
import com.minhtu.firesocialmedia.testutil.fakeIpInfoRemoteDataSource
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

private class InfoFakeAuthRepository : AuthenticationRepository {
    var saveResult = true
    override suspend fun clearAccount() {}
    override suspend fun signInWithEmailAndPassword(email: String, password: String): SignInError? = null
    override suspend fun saveAccountToLocalStorage(email: String, password: String) {}
    override suspend fun checkUserExists(email: String): SignInState = SignInState(false, null)
    override suspend fun checkLocalAccount(): Credentials? = null
    override suspend fun handleSignInGoogleResult(credential: Any): String? = null
    override suspend fun signUpWithEmailAndPassword(email: String, password: String): Result<Unit> = Result.success(Unit)
    override suspend fun fetchSignInMethodsForEmail(email: String): EmailExistResult = EmailExistResult(false, "")
    override suspend fun sendPasswordResetEmail(email: String): Boolean = true
    override suspend fun saveSignUpInformation(userInstance: UserInstance): Boolean = saveResult
}

private class InfoFakeUserRepository(private val uid: String?) : UserRepository {
    override suspend fun getUser(userId: String, isCurrentUser: Boolean): UserDTO? = null
    override suspend fun getCurrentUserUid(): String? = uid
}

private class InfoFakeAuthCryptoService(private val token: String) : AuthCryptoService {
    override fun saveAccount(email: String, password: String) {}
    override suspend fun loadAccount(): CredentialsDTO? = null
    override suspend fun clearAccount() {}
    override suspend fun getFCMToken(): String = token
}

private class InfoFakeAuthDatabaseService : AuthDatabaseService {
    var savedLoginActivityForUserId: String? = null
    override suspend fun saveLoginActivityInfo(
        userId: String,
        locationInfo: IpInfoResponseDTO,
        historyPath: String,
        loginHistoryPath: String
    ) {
        savedLoginActivityForUserId = userId
    }
    override suspend fun getUser(userId: String): UserDTO? = null
    override suspend fun saveSignUpInformation(user: UserDTO): Boolean = true
}

@OptIn(ExperimentalCoroutinesApi::class)
class InformationViewModelTest {
    private lateinit var authRepo: InfoFakeAuthRepository
    private lateinit var userRepo: InfoFakeUserRepository
    private lateinit var cryptoService: InfoFakeAuthCryptoService
    private lateinit var authDbService: InfoFakeAuthDatabaseService

    @BeforeTest
    fun setup() {
        authRepo = InfoFakeAuthRepository()
        userRepo = InfoFakeUserRepository("testUid")
        cryptoService = InfoFakeAuthCryptoService("testToken")
        authDbService = InfoFakeAuthDatabaseService()
    }

    private fun vm(
        dispatcher: CoroutineDispatcher,
        userRepository: UserRepository = userRepo
    ): InformationViewModel = InformationViewModel(
        SaveSignUpInformationUseCase(authRepo),
        GetCurrentUserUidUseCase(userRepository),
        GetFCMTokenUseCase(cryptoService),
        SaveLoginActivityInfoUseCase(authDbService, fakeIpInfoRemoteDataSource()),
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
        val viewModel = vm(dispatcher, userRepository = InfoFakeUserRepository(null))
        // Should complete without error, currentUserId remains null and nothing is saved
        viewModel.saveLoginActivityInfo()
        advanceUntilIdle()

        assertNull(authDbService.savedLoginActivityForUserId)
        assertNull(viewModel.addInformationStatus.value)
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

        assertEquals("testUid", authDbService.savedLoginActivityForUserId)
        assertEquals(true, viewModel.addInformationStatus.value)
    }

    @Test
    fun `resetAddInformationStatus sets status to null`() = runTest {
        val dispatcher = StandardTestDispatcher(testScheduler)
        val viewModel = vm(dispatcher)
        viewModel.updateEmail("e")
        viewModel.updateAvatar("a")
        viewModel.updateUsername("username")
        viewModel.finishSignUpStage()
        advanceUntilIdle()
        assertEquals(true, viewModel.addInformationStatus.value)

        viewModel.resetAddInformationStatus()

        assertNull(viewModel.addInformationStatus.value)
    }

    @Test
    fun `setPendingSignUpCredentials stores email and password`() {
        val viewModel = InformationViewModel(
            SaveSignUpInformationUseCase(authRepo),
            GetCurrentUserUidUseCase(userRepo),
            GetFCMTokenUseCase(cryptoService),
            SaveLoginActivityInfoUseCase(authDbService, fakeIpInfoRemoteDataSource())
        )
        viewModel.setPendingSignUpCredentials("a@b.com", "pw")

        assertEquals("a@b.com", viewModel.pendingSignUpEmail)
        assertEquals("pw", viewModel.pendingSignUpPassword)
    }
}
