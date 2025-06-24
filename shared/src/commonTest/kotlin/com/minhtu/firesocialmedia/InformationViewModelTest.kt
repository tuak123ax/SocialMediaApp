package com.minhtu.firesocialmedia

import com.minhtu.firesocialmedia.information.InformationViewModel
import com.minhtu.firesocialmedia.instance.UserInstance
import com.minhtu.firesocialmedia.utils.Utils
import io.mockative.coEvery
import io.mockative.eq
import io.mockative.every
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import io.mockative.any
import kotlinx.coroutines.test.advanceUntilIdle
import kotlin.test.assertEquals

@OptIn(ExperimentalCoroutinesApi::class)
class InformationViewModelTest {
    private val testDispatcher = StandardTestDispatcher()
    private lateinit var platform: PlatformContextMock
    private lateinit var authServiceMock : AuthServiceMock
    private lateinit var cryptoServiceMock : CryptoServiceMock
    private lateinit var firebaseServiceMock : FirebaseServiceMock
    private lateinit var databaseServiceMock : DatabaseServiceMock

    @BeforeTest
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        platform = PlatformContextMock()
        authServiceMock = AuthServiceMock()
        cryptoServiceMock = CryptoServiceMock()
        firebaseServiceMock = FirebaseServiceMock()
        databaseServiceMock = DatabaseServiceMock()
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `finish sign up with username blank`() = runTest(testDispatcher) {
        val informationViewModel = InformationViewModel(testDispatcher)

        informationViewModel.updateUsername("")

        informationViewModel.finishSignUpStage(platform)

        advanceUntilIdle()

        val status = informationViewModel.addInformationStatus.value
        assertEquals(false, status)
    }

    @Test
    fun `finish sign up with error from server`() = runTest(testDispatcher) {
        val informationViewModel = InformationViewModel(testDispatcher)

        every { platform.auth } returns authServiceMock
        every { platform.crypto } returns cryptoServiceMock
        every { platform.database } returns databaseServiceMock

        every { authServiceMock.getCurrentUserUid() } returns "testUid"
        coEvery { cryptoServiceMock.getFCMToken() } returns "testToken"

        informationViewModel.email = "email"
        informationViewModel.avatar = "avatar"
        informationViewModel.username = "username"

        val testUserInstance = UserInstance(informationViewModel.email,
            informationViewModel.avatar,
            informationViewModel.username,
            "",
            cryptoServiceMock.getFCMToken(),authServiceMock.getCurrentUserUid()!!, HashMap())
        coEvery { databaseServiceMock.saveSignUpInformation(eq(testUserInstance), any()) }
            .invokes { args ->
                val callback = args[1] as Utils.Companion.SaveSignUpInformationCallBack
                callback.onFailure()
            }
        informationViewModel.finishSignUpStage(platform)

        advanceUntilIdle()

        val status = informationViewModel.addInformationStatus.value
        assertEquals(false, status)
    }

    @Test
    fun `finish sign up success`() = runTest(testDispatcher) {
        val informationViewModel = InformationViewModel(testDispatcher)

        every { platform.auth } returns authServiceMock
        every { platform.crypto } returns cryptoServiceMock
        every { platform.database } returns databaseServiceMock

        every { authServiceMock.getCurrentUserUid() } returns "testUid"
        coEvery { cryptoServiceMock.getFCMToken() } returns "testToken"

        informationViewModel.updateEmail("email")
        informationViewModel.updateAvatar("avatar")
        informationViewModel.updateUsername("username")

        val testUserInstance = UserInstance(informationViewModel.email,
            informationViewModel.avatar,
            informationViewModel.username,
            "",
            cryptoServiceMock.getFCMToken(),authServiceMock.getCurrentUserUid()!!, HashMap())
        coEvery { databaseServiceMock.saveSignUpInformation(eq(testUserInstance), any()) }
            .invokes { args ->
                val callback = args[1] as Utils.Companion.SaveSignUpInformationCallBack
                callback.onSuccess()
            }
        informationViewModel.finishSignUpStage(platform)

        advanceUntilIdle()

        val status = informationViewModel.addInformationStatus.value
        assertEquals(true, status)
    }
}