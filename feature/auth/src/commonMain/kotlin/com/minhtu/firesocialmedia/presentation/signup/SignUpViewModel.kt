package com.minhtu.firesocialmedia.presentation.signup

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.minhtu.firesocialmedia.domain.entity.signup.SignUpState
import com.minhtu.firesocialmedia.domain.error.signup.SignUpError
import com.minhtu.firesocialmedia.domain.usecases.signup.SignUpUseCase
import com.rickclephas.kmp.observableviewmodel.ViewModel
import com.rickclephas.kmp.observableviewmodel.launch
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class SignUpViewModel(
    private val signUpUseCase: SignUpUseCase,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) : ViewModel() {
    private val _signUpStatus = MutableStateFlow(SignUpState())
    val signUpStatus = _signUpStatus.asStateFlow()

    fun resetSignUpStatus() {
        _signUpStatus.value = SignUpState()
    }

    var email by mutableStateOf("")
    fun updateEmail(input: String) { email = input }

    var password by mutableStateOf("")
    fun updatePassword(input: String) { password = input }

    var confirmPassword by mutableStateOf("")
    fun updateConfirmPassword(input: String) { confirmPassword = input }

    fun signUp() {
        viewModelScope.launch(ioDispatcher) {
            if (email.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
                _signUpStatus.value = SignUpState(false, SignUpError.DataEmptyError.message)
            } else {
                if (password != confirmPassword) {
                    _signUpStatus.value = SignUpState(false, SignUpError.PasswordMismatchError.message)
                } else {
                    if (password.length < 6) {
                        _signUpStatus.value = SignUpState(false, SignUpError.PasswordShortError.message)
                    } else {
                        val result = signUpUseCase.invoke(email, password)
                        if (result.isSuccess) {
                            _signUpStatus.value = SignUpState(true, "")
                        } else {
                            val error = result.exceptionOrNull()
                            if (error != null && error.message != null && error.message!!.isNotEmpty()) {
                                _signUpStatus.value = SignUpState(false, error.message!!)
                            } else {
                                _signUpStatus.value = SignUpState(false, SignUpError.Unknown("Sign up failed. Something went wrong!").message)
                            }
                        }
                    }
                }
            }
        }
    }
}
