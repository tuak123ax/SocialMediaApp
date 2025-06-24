package com.minhtu.firesocialmedia.forgotpassword

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.rickclephas.kmp.observableviewmodel.ViewModel
import com.minhtu.firesocialmedia.PlatformContext
import com.minhtu.firesocialmedia.constants.Constants
import com.minhtu.firesocialmedia.utils.Utils
import com.rickclephas.kmp.observableviewmodel.launch
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext

class ForgotPasswordViewModel(
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) : ViewModel() {
    var email by mutableStateOf("")
    fun updateEmail(input : String){
        email = input
    }

    private var _emailExisted = MutableStateFlow<Pair<Boolean, String>?>(null)
    var emailExisted = _emailExisted.asStateFlow()
    fun checkIfEmailExists(platform: PlatformContext) {
        viewModelScope.launch {
            withContext(ioDispatcher) {
                if(email.isNotEmpty()) {
                    platform.auth.fetchSignInMethodsForEmail(email,
                        object : Utils.Companion.FetchSignInMethodCallback{
                            override fun onSuccess(result: Pair<Boolean, String>) {
                                _emailExisted.value = result
                            }

                            override fun onFailure(result: Pair<Boolean, String>) {
                                _emailExisted.value = result
                            }

                        })
                } else {
                    _emailExisted.value = Pair(false, Constants.EMAIL_EMPTY)
                }
            }
        }
    }

    private var _emailSent = MutableStateFlow<Boolean?>(null)
    var emailSent = _emailSent.asStateFlow()
    fun sendEmailResetPassword(platform: PlatformContext) {
        viewModelScope.launch {
            withContext(ioDispatcher) {
                platform.auth.sendPasswordResetEmail(email,
                    object : Utils.Companion.SendPasswordResetEmailCallback{
                        override fun onSuccess() {
                            _emailSent.value = true
                        }

                        override fun onFailure() {
                            _emailSent.value = false
                        }

                    })
            }
        }
    }
    fun resetEmailResetPassword(){
        _emailExisted.value = null
        _emailSent.value = null
    }
}