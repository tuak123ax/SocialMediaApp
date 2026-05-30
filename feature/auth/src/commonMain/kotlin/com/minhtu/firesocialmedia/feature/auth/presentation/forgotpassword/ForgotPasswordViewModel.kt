package com.minhtu.firesocialmedia.feature.auth.presentation.forgotpassword

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.minhtu.firesocialmedia.core.constants.Constants
import com.minhtu.firesocialmedia.core.domain.entity.forgotpassword.EmailExistResult
import com.minhtu.firesocialmedia.core.domain.usecases.forgotpassword.CheckIfEmailExistsUseCase
import com.minhtu.firesocialmedia.core.domain.usecases.forgotpassword.SendEmailResetPasswordUseCase
import com.rickclephas.kmp.observableviewmodel.ViewModel
import com.rickclephas.kmp.observableviewmodel.launch
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class ForgotPasswordViewModel(
    private val checkIfEmailExistsUseCase: CheckIfEmailExistsUseCase,
    private val sendEmailResetPasswordUseCase: SendEmailResetPasswordUseCase,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) : ViewModel() {
    var email by mutableStateOf("")
    fun updateEmail(input: String) {
        email = input
    }

    private var _emailExisted = MutableStateFlow<EmailExistResult?>(null)
    var emailExisted = _emailExisted.asStateFlow()
    fun checkIfEmailExists() {
        viewModelScope.launch(ioDispatcher) {
            if (email.isNotEmpty()) {
                _emailExisted.value = checkIfEmailExistsUseCase.invoke(email)
            } else {
                _emailExisted.value = EmailExistResult(false, Constants.EMAIL_EMPTY)
            }
        }
    }

    private var _emailSent = MutableStateFlow<Boolean?>(null)
    var emailSent = _emailSent.asStateFlow()
    suspend fun sendEmailResetPassword() {
        _emailSent.value = sendEmailResetPasswordUseCase.invoke(email)
    }

    fun resetEmailExistStatus() {
        _emailExisted.value = null
    }

    fun resetEmailResetPassword() {
        _emailSent.value = null
    }
}
