package com.minhtu.firesocialmedia.presentation.twofa

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.minhtu.firesocialmedia.domain.entity.authentication.TwoFAResponse
import com.minhtu.firesocialmedia.security.data.remote.dto.user.UserDTO
import com.minhtu.firesocialmedia.security.entity.user.toSecurityUser
import com.minhtu.firesocialmedia.domain.usecases.home.security.ClearAccountUseCase
import com.minhtu.firesocialmedia.domain.usecases.settings.Enable2FAUseCase
import com.minhtu.firesocialmedia.domain.usecases.settings.Verify2FAUseCase
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class VerifyOTPViewModel(
    private val enable2FAUseCase: Enable2FAUseCase,
    private val verify2FAUseCase: Verify2FAUseCase,
    private val clearAccountUseCase: ClearAccountUseCase,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) : ViewModel() {
    private var _otpToVerify = MutableStateFlow("")
    var otpToVerify = _otpToVerify.asStateFlow()

    fun updateOtpToVerify(input: String) {
        if (input.length <= 6) {
            _otpToVerify.value = input
        }
    }

    private var _verifyOTPResult = MutableStateFlow<TwoFAResponse?>(null)
    var verifyOTPResult = _verifyOTPResult.asStateFlow()

    fun enableOTP(currentUser: UserDTO, secret: String, otpToVerify: String) {
        viewModelScope.launch(ioDispatcher) {
            _verifyOTPResult.value = enable2FAUseCase.invoke(currentUser.toSecurityUser(), secret, otpToVerify)
        }
    }

    fun verifyOTP(currentUser: UserDTO, otpToVerify: String) {
        viewModelScope.launch(ioDispatcher) {
            _verifyOTPResult.value = verify2FAUseCase.invoke(currentUser.toSecurityUser(), otpToVerify)
        }
    }

    fun resetVerifyOTPResult() {
        _verifyOTPResult.value = null
    }

    fun resetOtp() {
        _otpToVerify.value = ""
    }

    fun clearAccountInLocalData() {
        viewModelScope.launch(ioDispatcher) {
            clearAccountUseCase.invoke()
        }
    }
}

