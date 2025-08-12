package com.minhtu.firesocialmedia.presentation.signin

import com.minhtu.firesocialmedia.constants.Constants
import com.minhtu.firesocialmedia.data.model.signin.SignInState
import com.minhtu.firesocialmedia.di.PlatformContext
import com.minhtu.firesocialmedia.domain.serviceimpl.signinlauncher.SignInLauncher
import com.minhtu.firesocialmedia.utils.Utils
import com.rickclephas.kmp.observableviewmodel.ViewModel
import com.rickclephas.kmp.observableviewmodel.launch
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext

class SignInViewModel(
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) : ViewModel() {
    private var launcher: SignInLauncher? = null

    fun setSignInLauncher(launcher: SignInLauncher) {
        this.launcher = launcher
    }

    private var _signInStatus = MutableStateFlow(SignInState())
    var signInState = _signInStatus.asStateFlow()

    fun updateSignInStatus(state : SignInState) {
        _signInStatus.value = state
    }
    fun resetSignInStatus() {
        _signInStatus.value = SignInState()
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

    fun signIn(showLoading : () -> Unit, platform: PlatformContext) {
        viewModelScope.launch {
            withContext(ioDispatcher) {
                if (email.value.isBlank() || password.value.isBlank()) {
                    _signInStatus.value = SignInState(false, Constants.DATA_EMPTY)
                } else {
                    showLoading()
                    email.value = email.value.lowercase()
                    val result = platform.auth.signInWithEmailAndPassword(email.value, password.value)
                    if (result.isSuccess) {
                        if (rememberPassword.value) {
                            platform.crypto.saveAccount(email.value, password.value)
                        }
                        checkEmailInDatabase(email.value, platform)
                    } else {
                        _signInStatus.value = SignInState(false, Constants.LOGIN_ERROR)
                    }
                }
            }
        }
    }

    private fun checkEmailInDatabase(email: String, platform: PlatformContext) {
        viewModelScope.launch {
            withContext(ioDispatcher) {
                platform.database.checkUserExists(email) { result ->
                    _signInStatus.value = result
                }
            }
        }
    }

    fun checkLocalAccount(platform: PlatformContext, showLoading : () -> Unit) {
        viewModelScope.launch {
            withContext(ioDispatcher) {
                val creds = platform.crypto.loadAccount()
                if (creds != null) {
                    updateEmail(creds.email)
                    updatePassword(creds.password)
                    signIn(showLoading, platform)
                }
            }
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

    fun handleSignInResult(credential : Any, platform: PlatformContext) {
        viewModelScope.launch {
            withContext(ioDispatcher) {
                platform.auth.handleSignInGoogleResult(credential,
                    object : Utils.Companion.SignInGoogleCallback{
                        override fun onSuccess(email: String) {
                            checkEmailInDatabase(email, platform)
                        }
                        override fun onFailure() {
                            _signInStatus.value = SignInState(false, Constants.LOGIN_ERROR)
                        }
                    })
            }
        }
    }
}