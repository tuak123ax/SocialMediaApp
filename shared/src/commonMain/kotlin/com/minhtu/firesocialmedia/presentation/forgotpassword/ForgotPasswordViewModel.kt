package com.minhtu.firesocialmedia.presentation.forgotpassword

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.minhtu.firesocialmedia.constants.Constants
import com.minhtu.firesocialmedia.domain.entity.forgotpassword.EmailExistResult
import com.minhtu.firesocialmedia.domain.usecases.forgotpassword.CheckIfEmailExistsUseCase
import com.minhtu.firesocialmedia.domain.usecases.forgotpassword.SendEmailResetPasswordUseCase
import com.rickclephas.kmp.observableviewmodel.ViewModel
import com.rickclephas.kmp.observableviewmodel.launch
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext

class ForgotPasswordViewModel(
    private val checkIfEmailExistsUseCase: CheckIfEmailExistsUseCase,
    private val sendEmailResetPasswordUseCase : SendEmailResetPasswordUseCase,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) : ViewModel() {
    var email by mutableStateOf("")
    fun updateEmail(input : String){
        email = input
    }

    private var _emailExisted = MutableStateFlow<EmailExistResult?>(null)
    var emailExisted = _emailExisted.asStateFlow()
    fun checkIfEmailExists() {
        viewModelScope.launch {
            withContext(ioDispatcher) {
                if(email.isNotEmpty()) {
                    val result = checkIfEmailExistsUseCase.invoke(email)
                    _emailExisted.value = result
                } else {
                    _emailExisted.value = EmailExistResult(false, Constants.EMAIL_EMPTY)
                }
            }
        }
    }

    private var _emailSent = MutableStateFlow<Boolean?>(null)
    var emailSent = _emailSent.asStateFlow()
    suspend fun sendEmailResetPassword() {
        val result = sendEmailResetPasswordUseCase.invoke(email)
        _emailSent.value = result
    }
    fun resetEmailResetPassword(){
        _emailExisted.value = null
        _emailSent.value = null
    }
}