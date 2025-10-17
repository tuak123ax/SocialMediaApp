package com.minhtu.firesocialmedia.presentation.signin

import androidx.compose.runtime.mutableStateOf
import com.minhtu.firesocialmedia.data.remote.service.signinlauncher.SignInLauncher
import com.minhtu.firesocialmedia.domain.entity.crypto.Credentials
import com.minhtu.firesocialmedia.domain.entity.signin.SignInState
import com.minhtu.firesocialmedia.domain.error.signin.SignInError
import com.minhtu.firesocialmedia.domain.usecases.signin.CheckLocalAccountUseCase
import com.minhtu.firesocialmedia.domain.usecases.signin.CheckUserExistsUseCase
import com.minhtu.firesocialmedia.domain.usecases.signin.HandleSignInGoogleResultUseCase
import com.minhtu.firesocialmedia.domain.usecases.signin.RememberPasswordUseCase
import com.minhtu.firesocialmedia.domain.usecases.signin.SignInUseCase
import com.minhtu.firesocialmedia.platform.logMessage
import com.rickclephas.kmp.observableviewmodel.ViewModel
import com.rickclephas.kmp.observableviewmodel.launch
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext

class SignInViewModel(
    private val signInUseCase : SignInUseCase,
    private val rememberPasswordUseCase: RememberPasswordUseCase,
    private val checkUserExistsUseCase: CheckUserExistsUseCase,
    private val checkLocalAccountUseCase : CheckLocalAccountUseCase,
    private val handleSignInGoogleResult: HandleSignInGoogleResultUseCase,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) : ViewModel() {
    private var launcher: SignInLauncher? = null

    fun setSignInLauncher(launcher: SignInLauncher) {
        this.launcher = launcher
    }

    private var _signInStatus = MutableStateFlow(SignInState(false,null))
    var signInState = _signInStatus.asStateFlow()

    fun updateSignInStatus(state : SignInState) {
        _signInStatus.value = state
    }
    fun resetSignInStatus() {
        _signInStatus.value = SignInState(false, null)
    }
    var _rememberPassword = MutableStateFlow(false)
    var rememberPassword = _rememberPassword.asStateFlow()
    fun updateRememberPassword(checked : Boolean){
        _rememberPassword.value = checked
    }
    var email = MutableStateFlow("")
    fun updateEmail(input : String){
        email.value = input
    }

    var password = MutableStateFlow("")
    fun updatePassword(input : String){
        password.value =  input
    }

    fun signIn(showLoading : () -> Unit) {
        viewModelScope.launch {
            withContext(ioDispatcher) {
                if (email.value.isBlank() || password.value.isBlank()) {
                    _signInStatus.value = SignInState(false, SignInError.DataEmpty)
                } else {
                    showLoading()
                    email.value = email.value.lowercase()
                    val signInError = signInUseCase.invoke(email.value, password.value)
                    if (signInError == null) {
                        if (rememberPassword.value) {
                            rememberPasswordUseCase.invoke(email.value, password.value)
                        }
                        checkEmailInDatabase(email.value)
                    } else {
                        logMessage("signIn", { "Error when sign in" })
                        _signInStatus.value = SignInState(false, signInError)
                    }
                }
            }
        }
    }

    private suspend fun checkEmailInDatabase(email: String) {
        val result = checkUserExistsUseCase.invoke(email)
        _signInStatus.value = result
    }

    val localCredentials = mutableStateOf<Credentials?>(null)
    suspend fun checkLocalAccount() {
        localCredentials.value = checkLocalAccountUseCase.invoke()
        if (localCredentials.value != null) {
            updateEmail(localCredentials.value!!.email)
            updatePassword(localCredentials.value!!.password)
        }
    }

    //-----------Sign in with Google------------//
    fun signInWithGoogle(){
        viewModelScope.launch {
            withContext(ioDispatcher) {
                launcher?.launchGoogleSignIn()
            }
        }
    }

    fun handleSignInResult(credential : Any) {
        viewModelScope.launch {
            withContext(ioDispatcher) {
                val result = handleSignInGoogleResult.invoke(credential)
                if(!result.isNullOrEmpty()) {
                    checkEmailInDatabase(result)
                } else {
                    _signInStatus.value = SignInState(false, null)
                }
            }
        }
    }

    fun reset() {
        resetSignInStatus()
        updateEmail("")
        updatePassword("")
        _rememberPassword.value = false
    }
}