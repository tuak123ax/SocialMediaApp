package com.minhtu.firesocialmedia.forgotpassword

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.rickclephas.kmp.observableviewmodel.ViewModel
import com.minhtu.firesocialmedia.PlatformContext
import com.minhtu.firesocialmedia.constants.Constants
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class ForgotPasswordViewModel : ViewModel() {
    var email by mutableStateOf("")
    fun updateEmail(input : String){
        email = input
    }

    private var _emailExisted = MutableStateFlow<Pair<Boolean, String>?>(null)
    var emailExisted = _emailExisted.asStateFlow()
    fun checkIfEmailExists(platform: PlatformContext) {
        if(email.isNotEmpty()) {
            platform.auth.fetchSignInMethodsForEmail(email, _emailExisted)
        } else {
            _emailExisted.value = Pair(false, Constants.EMAIL_EMPTY)
        }
    }

    private var _emailSent = MutableStateFlow<Boolean?>(null)
    var emailSent = _emailSent.asStateFlow()
    fun sendEmailResetPassword(platform: PlatformContext) {
        platform.auth.sendPasswordResetEmail(email, _emailSent)
    }
    fun resetEmailResetPassword(){
        _emailExisted.value = null
        _emailSent.value = null
    }
}