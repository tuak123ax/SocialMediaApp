package com.minhtu.firesocialmedia

import com.minhtu.firesocialmedia.constants.Constants
import com.minhtu.firesocialmedia.di.AuthServiceMock
import com.minhtu.firesocialmedia.di.Credentials
import com.minhtu.firesocialmedia.di.CryptoServiceMock
import com.minhtu.firesocialmedia.di.DatabaseServiceMock
import com.minhtu.firesocialmedia.di.FirebaseServiceMock
import com.minhtu.firesocialmedia.di.PlatformContextMock
import com.minhtu.firesocialmedia.platform.SignInLauncherMock
import com.minhtu.firesocialmedia.data.model.signin.SignInState
import com.minhtu.firesocialmedia.presentation.signin.SignInViewModel
import com.minhtu.firesocialmedia.utils.Utils
import io.mockative.coEvery
import io.mockative.eq
import io.mockative.every
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

@Suppress("UNCHECKED_CAST")
@OptIn(ExperimentalCoroutinesApi::class)
class SignInViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var platform: PlatformContextMock
    private lateinit var authServiceMock : AuthServiceMock
    private lateinit var cryptoServiceMock : CryptoServiceMock
    private lateinit var firebaseServiceMock : FirebaseServiceMock
    private lateinit var databaseServiceMock : DatabaseServiceMock
    private lateinit var signInLauncher: SignInLauncherMock
    @BeforeTest
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        platform = PlatformContextMock()
        authServiceMock = AuthServiceMock()
        cryptoServiceMock = CryptoServiceMock()
        firebaseServiceMock = FirebaseServiceMock()
        databaseServiceMock = DatabaseServiceMock()
        signInLauncher = SignInLauncherMock()
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `signIn with blank email shows error`() = runTest(testDispatcher) {
        val viewModel = SignInViewModel(testDispatcher)
        viewModel.resetSignInStatus()
        viewModel.updateEmail("")
        viewModel.updatePassword("somepassword")

        viewModel.signIn(showLoading = {}, platform = platform)

        advanceUntilIdle() // works because test dispatcher is used consistently

        val signInState = viewModel.signInState.value
        assertEquals(false, signInState.signInStatus)
        assertEquals(Constants.DATA_EMPTY, signInState.message)
    }

    @Test
    fun `signIn with blank password shows error`() = runTest(testDispatcher) {
        val viewModel = SignInViewModel(testDispatcher)
        viewModel.resetSignInStatus()
        viewModel.updateEmail("test1234@gmail.com")
        viewModel.updatePassword("")

        viewModel.signIn(showLoading = {}, platform = platform)

        advanceUntilIdle()

        val signInState = viewModel.signInState.value
        assertEquals(false, signInState.signInStatus)
        assertEquals(Constants.DATA_EMPTY, signInState.message)
    }

    @Test
    fun `signIn with both email and password are blank shows error`() = runTest(testDispatcher) {
        val viewModel = SignInViewModel(testDispatcher)
        viewModel.resetSignInStatus()
        viewModel.updateEmail("")
        viewModel.updatePassword("")

        viewModel.signIn(showLoading = {}, platform = platform)

        advanceUntilIdle()

        val signInState = viewModel.signInState.value
        assertEquals(false, signInState.signInStatus)
        assertEquals(Constants.DATA_EMPTY, signInState.message)
    }

    @Test
    fun `signIn with valid email and password triggers success flow`() = runTest(testDispatcher) {
        val email = "test@gmail.com"
        val password = "securepassword"

        val viewModel = SignInViewModel(
            testDispatcher
        )
        viewModel.resetSignInStatus()
        viewModel.updateEmail(email.lowercase())
        viewModel.updatePassword(password)
        viewModel.updateRememberPassword(true)

        // Stub methods
        every { platform.auth } returns authServiceMock
        every { platform.crypto } returns cryptoServiceMock
        every { platform.firebase } returns firebaseServiceMock
        every { platform.database } returns databaseServiceMock

        coEvery { authServiceMock.signInWithEmailAndPassword(viewModel.email.value, password) } returns Result.success(Unit)
        every { cryptoServiceMock.saveAccount(email, password) } returns Unit
        every { firebaseServiceMock.checkUserExists(eq(email), any()) }
            .invokes { args ->
                val callback = args[1] as (SignInState) -> Unit
                callback(SignInState(true, ""))
            }

        // Act
        viewModel.signIn(showLoading = {}, platform = platform)
        advanceUntilIdle()

        // Assert
        val signInState = viewModel.signInState.value
        assertEquals(true,signInState.signInStatus)
    }

    @Test
    fun `signIn with invalid email and password triggers fail flow`() = runTest(testDispatcher) {
        val email = "wrongTestAccount@gmail.com"
        val password = "securepassword"

        val platform = PlatformContextMock()
        val viewModel = SignInViewModel(
            testDispatcher
        )
        viewModel.resetSignInStatus()
        viewModel.updateEmail(email.lowercase())
        viewModel.updatePassword(password)

        // Stub methods

        every { platform.auth } returns authServiceMock
        every { platform.crypto } returns cryptoServiceMock
        every { platform.firebase } returns firebaseServiceMock
        every { platform.database } returns databaseServiceMock

        coEvery { authServiceMock.signInWithEmailAndPassword(viewModel.email.value, password) } returns Result.failure(
            Exception("Login failed"))

        // Act
        viewModel.signIn(showLoading = {}, platform = platform)
        advanceUntilIdle()

        // Assert
        val signInState = viewModel.signInState.value
        assertEquals(false,signInState.signInStatus)
    }

    @Test
    fun `login with account that is in local storage triggers success flow`() = runTest(testDispatcher) {
        val platform = PlatformContextMock()
        val viewModel = SignInViewModel(testDispatcher)

        every { platform.auth } returns authServiceMock
        every { platform.crypto } returns cryptoServiceMock
        every { platform.firebase } returns firebaseServiceMock
        every { platform.database } returns databaseServiceMock

        val correctCredentials = Credentials("correctuser@gmail.com", "123321")

        coEvery { cryptoServiceMock.loadAccount() } returns correctCredentials
        viewModel.updateEmail(correctCredentials.email.lowercase())
        viewModel.updatePassword(correctCredentials.password)
        coEvery { authServiceMock.signInWithEmailAndPassword(viewModel.email.value, viewModel.password.value) } returns Result.success(Unit)
        every { cryptoServiceMock.saveAccount(viewModel.email.value, viewModel.password.value) } returns Unit
        every { firebaseServiceMock.checkUserExists(eq(viewModel.email.value), any()) }
            .invokes { args ->
                val callback = args[1] as (SignInState) -> Unit
                callback(SignInState(true, ""))
            }

        viewModel.checkLocalAccount(platform, showLoading = {})
        advanceUntilIdle()

        val signInState = viewModel.signInState.value
        assertEquals(true,signInState.signInStatus)
    }

    @Test
    fun `login with Google triggers success flow`() = runTest(testDispatcher) {
        val platform = PlatformContextMock()
        val viewModel = SignInViewModel(testDispatcher)

        every { platform.auth } returns authServiceMock
        every { platform.crypto } returns cryptoServiceMock
        every { platform.firebase } returns firebaseServiceMock
        every { platform.database } returns databaseServiceMock

        viewModel.setSignInLauncher(signInLauncher)

        every { signInLauncher.launchGoogleSignIn() } returns Unit
        viewModel.signInWithGoogle()

        val correctCredential = Any()
        every { authServiceMock.handleSignInGoogleResult(eq(correctCredential), any()) }
            .invokes { args ->
                val callback = args[1] as Utils.Companion.SignInGoogleCallback
                callback.onSuccess("correctemail@gmail.com")
            }

        every { firebaseServiceMock.checkUserExists(eq("correctemail@gmail.com"), any()) }
            .invokes { args ->
                val callback = args[1] as (SignInState) -> Unit
                callback(SignInState(true, ""))
            }

        viewModel.handleSignInResult(correctCredential, platform)

        advanceUntilIdle()

        val signInState = viewModel.signInState.value
        assertEquals(true,signInState.signInStatus)
    }

    @Test
    fun `login with Google triggers fail flow`() = runTest(testDispatcher) {
        val platform = PlatformContextMock()
        val viewModel = SignInViewModel(testDispatcher)

        every { platform.auth } returns authServiceMock
        every { platform.crypto } returns cryptoServiceMock
        every { platform.firebase } returns firebaseServiceMock
        every { platform.database } returns databaseServiceMock

        viewModel.setSignInLauncher(signInLauncher)

        every { signInLauncher.launchGoogleSignIn() } returns Unit
        viewModel.signInWithGoogle()

        val wrongCredential = Any()
        every { authServiceMock.handleSignInGoogleResult(eq(wrongCredential), any()) }
            .invokes { args ->
                val callback = args[1] as Utils.Companion.SignInGoogleCallback
                callback.onFailure()
            }

        viewModel.handleSignInResult(wrongCredential, platform)

        advanceUntilIdle()

        val signInState = viewModel.signInState.value
        assertEquals(false,signInState.signInStatus)
        assertEquals(Constants.LOGIN_ERROR, signInState.message)
    }
}

