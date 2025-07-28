package com.minhtu.firesocialmedia.presentation.signup

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.minhtu.firesocialmedia.constants.Constants
import com.minhtu.firesocialmedia.data.model.signup.SignUpState
import com.minhtu.firesocialmedia.di.PlatformContext
import com.rickclephas.kmp.observableviewmodel.ViewModel
import com.rickclephas.kmp.observableviewmodel.launch
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext

class SignUpViewModel(
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) : ViewModel() {
    private val _signUpStatus = MutableStateFlow(SignUpState())
    val signUpStatus = _signUpStatus.asStateFlow()

    fun resetSignUpStatus() {
        _signUpStatus.value = SignUpState()
    }

    var email by mutableStateOf("")
    fun updateEmail(input : String){
        email = input
    }

    var password by mutableStateOf("")
    fun updatePassword(input : String){
        password =  input
    }

    var confirmPassword by mutableStateOf("")
    fun updateConfirmPassword(input : String){
        confirmPassword =  input
    }

    fun signUp(platform : PlatformContext){
        viewModelScope.launch {
            withContext(ioDispatcher) {
                if(email.isEmpty() || password.isEmpty() || confirmPassword.isEmpty())
                {
                    _signUpStatus.value = SignUpState(false, Constants.DATA_EMPTY)
                } else{
                    if(password != confirmPassword){
                        _signUpStatus.value = SignUpState(false, Constants.PASSWORD_MISMATCH)
                    } else{
                        if(password.length < 6){
                            _signUpStatus.value = SignUpState(false, Constants.PASSWORD_SHORT)
                        } else{
                            val result = platform.auth.signUpWithEmailAndPassword(email, password)
                            if(result.isSuccess) {
                                _signUpStatus.value = SignUpState(true, "")
                            } else {
                                _signUpStatus.value = SignUpState(false, Constants.SIGNUP_FAIL)
                            }
                        }
                    }
                }
            }
        }
    }
}