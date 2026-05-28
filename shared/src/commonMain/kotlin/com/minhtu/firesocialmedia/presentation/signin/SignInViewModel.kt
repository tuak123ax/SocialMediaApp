package com.minhtu.firesocialmedia.presentation.signin

import androidx.compose.runtime.mutableStateOf
import com.minhtu.firesocialmedia.data.remote.service.signinlauncher.SignInLauncher
import com.minhtu.firesocialmedia.core.domain.entity.crypto.Credentials
import com.minhtu.firesocialmedia.core.domain.entity.signin.SignInState
import com.minhtu.firesocialmedia.core.domain.entity.user.UserInstance
import com.minhtu.firesocialmedia.core.domain.error.signin.SignInError
import com.minhtu.firesocialmedia.core.domain.usecases.common.GetCurrentUserUidUseCase
import com.minhtu.firesocialmedia.core.domain.usecases.common.GetUserUseCase
import com.minhtu.firesocialmedia.core.domain.usecases.signin.CheckLocalAccountUseCase
import com.minhtu.firesocialmedia.core.domain.usecases.signin.CheckUserExistsUseCase
import com.minhtu.firesocialmedia.core.domain.usecases.signin.HandleSignInGoogleResultUseCase
import com.minhtu.firesocialmedia.core.domain.usecases.signin.RememberPasswordUseCase
import com.minhtu.firesocialmedia.core.domain.usecases.signin.SaveLoginActivityInfoUseCase
import com.minhtu.firesocialmedia.core.domain.usecases.signin.SignInUseCase
import com.minhtu.firesocialmedia.platform.logMessage
import com.rickclephas.kmp.observableviewmodel.ViewModel
import com.rickclephas.kmp.observableviewmodel.launch
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class SignInViewModel(
    private val signInUseCase: SignInUseCase,
    private val rememberPasswordUseCase: RememberPasswordUseCase,
    private val checkUserExistsUseCase: CheckUserExistsUseCase,
    private val checkLocalAccountUseCase: CheckLocalAccountUseCase,
    private val handleSignInGoogleResult: HandleSignInGoogleResultUseCase,
    private val getCurrentUserUidUseCase: GetCurrentUserUidUseCase,
    private val getUserUseCase: GetUserUseCase,
    private val saveLoginActivityInfoUseCase : SaveLoginActivityInfoUseCase,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) : ViewModel() {
    private var launcher: SignInLauncher? = null

    fun setSignInLauncher(launcher: SignInLauncher) {
        this.launcher = launcher
    }

    private var _signInStatus = MutableStateFlow(SignInState(false, null))
    var signInState = _signInStatus.asStateFlow()

    fun updateSignInStatus(state: SignInState) {
        _signInStatus.value = state
    }

    fun resetSignInStatus() {
        _signInStatus.value = SignInState(false, null)
    }

    var _rememberPassword = MutableStateFlow(false)
    var rememberPassword = _rememberPassword.asStateFlow()
    fun updateRememberPassword(checked: Boolean) {
        _rememberPassword.value = checked
    }

    var email = MutableStateFlow("")
    fun updateEmail(input: String) {
        email.value = input
    }

    var password = MutableStateFlow("")
    fun updatePassword(input: String) {
        password.value = input
    }

    var currentUser = mutableStateOf<UserInstance?>(null)

    fun signIn(showLoading: () -> Unit) {
        viewModelScope.launch(ioDispatcher) {
            if (email.value.isBlank() || password.value.isBlank()) {
                _signInStatus.value = SignInState(false, SignInError.DataEmpty)
                return@launch
            }
            showLoading()
            email.value = email.value.lowercase()
            val signInError = signInUseCase.invoke(email.value, password.value)
            if (signInError == null) {
                if (rememberPassword.value) {
                    rememberPasswordUseCase.invoke(email.value, password.value)
                }
                checkEmailInDatabase(email.value)
            } else {
                _signInStatus.value = SignInState(false, signInError)
            }
        }
    }

    private suspend fun checkEmailInDatabase(email: String) {
        val result = checkUserExistsUseCase.invoke(email)
        _signInStatus.value = result
    }

    val localCredentials = mutableStateOf<Credentials?>(null)
    fun checkLocalAccount() {
        viewModelScope.launch(ioDispatcher) {
            localCredentials.value = checkLocalAccountUseCase.invoke()
            if (localCredentials.value != null) {
                updateEmail(localCredentials.value!!.email)
                updatePassword(localCredentials.value!!.password)
            }
        }
    }

    //-----------Sign in with Google------------//
    fun signInWithGoogle() {
        viewModelScope.launch(ioDispatcher) { launcher?.launchGoogleSignIn() }
    }

    fun handleSignInResult(credential: Any) {
        viewModelScope.launch(ioDispatcher) {
            val result = handleSignInGoogleResult.invoke(credential)
            if (!result.isNullOrEmpty()) {
                checkEmailInDatabase(result)
            } else {
                _signInStatus.value = SignInState(false, null)
            }
        }
    }

    fun reset() {
        resetSignInStatus()
        updateEmail("")
        updatePassword("")
        _rememberPassword.value = false
        currentUser.value = null
    }

    private var _check2FAStatus = MutableStateFlow<Boolean?>(null)
    var check2FAStatus = _check2FAStatus.asStateFlow()
    fun check2FAStatus() {
        viewModelScope.launch(ioDispatcher) {
            val currentUserId = getCurrentUserUidUseCase.invoke()
            if (currentUserId != null) {
                val user = getUserUseCase.invoke(currentUserId, true)
                if(user != null) {
                    currentUser.value = user
                    _check2FAStatus.value = user.twoFAEnabled
                    // Login successfully, track this activity after getting current user info
                    saveLoginActivityInfo(user)
                }
            }
        }
    }

    fun resetCheck2FAStatus() {
        _check2FAStatus.value = null
    }

    private suspend fun saveLoginActivityInfo(user : UserInstance) {
        logMessage("saveLoginActivityInfo", { "start save login activity info" })
        saveLoginActivityInfoUseCase.invoke(user.uid)
    }
}