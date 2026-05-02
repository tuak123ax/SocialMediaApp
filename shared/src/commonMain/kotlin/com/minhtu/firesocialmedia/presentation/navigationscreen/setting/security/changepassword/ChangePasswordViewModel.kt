package com.minhtu.firesocialmedia.presentation.navigationscreen.setting.security.changepassword

import com.minhtu.firesocialmedia.domain.entity.settings.ChangePasswordState
import com.minhtu.firesocialmedia.domain.entity.user.UserInstance
import com.minhtu.firesocialmedia.domain.error.changepassword.ChangePasswordError
import com.minhtu.firesocialmedia.domain.usecases.settings.ChangePasswordUseCase
import com.minhtu.firesocialmedia.domain.usecases.settings.ValidateNewPasswordUseCase
import com.minhtu.firesocialmedia.domain.usecases.settings.VerifyCurrentPasswordUseCase
import com.rickclephas.kmp.observableviewmodel.ViewModel
import com.rickclephas.kmp.observableviewmodel.launch
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlin.time.Clock.System
import kotlin.time.ExperimentalTime

class ChangePasswordViewModel(
    private val verifyCurrentPasswordUseCase : VerifyCurrentPasswordUseCase,
    private val validateNewPasswordUseCase : ValidateNewPasswordUseCase,
    private val changePasswordUseCase : ChangePasswordUseCase,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) : ViewModel() {
    private var _currentPassword = MutableStateFlow("")
    var currentPassword = _currentPassword.asStateFlow()
    fun updateCurrentPassword(input : String) {
        _currentPassword.value = input
    }
    private var _newPassword = MutableStateFlow("")
    var newPassword = _newPassword.asStateFlow()
    fun updateNewPassword(input : String) {
        _newPassword.value = input
    }
    private var _confirmPassword = MutableStateFlow("")
    var confirmPassword = _confirmPassword.asStateFlow()
    fun updateConfirmPassword(input : String) {
        _confirmPassword.value = input
    }

    private var _changePasswordState = MutableStateFlow<ChangePasswordState?>(null)
    var changePasswordState = _changePasswordState.asStateFlow()
    @OptIn(ExperimentalTime::class)
    fun updatePassword(currentUser : UserInstance) {
        viewModelScope.launch(ioDispatcher) {
            //Validate new password and confirm password
            val validateNewPasswordResult = validateNewPasswordUseCase.invoke(_newPassword.value, _confirmPassword.value)
            //Update password
            if(validateNewPasswordResult.isValid) {
                //Verify current password
                val verifyCurrentPasswordResult = verifyCurrentPasswordUseCase.invoke(currentUser.email ,_currentPassword.value)
                if(!verifyCurrentPasswordResult) {
                    _changePasswordState.value = ChangePasswordState(false, ChangePasswordError.CurrentPasswordWrongError)
                } else {
                    _changePasswordState.value = changePasswordUseCase.invoke(currentUser, _newPassword.value)
                    if(_changePasswordState.value != null && _changePasswordState.value!!.isValid) {
                        //Change password success
                        //Reset retry flag
                        hasRetried = false
                        //Update data for user
                        currentUser.lastTimeChangePassword = System.now().toEpochMilliseconds()
                    }
                }
            } else {
                _changePasswordState.value = validateNewPasswordResult
            }
        }
    }
    fun resetChangePasswordState() {
        _changePasswordState.value = null
    }

    private var hasRetried = false
    fun retryWithReAuth(currentUser: UserInstance) = viewModelScope.launch {
        // Prevent infinite retry
        if (hasRetried) {
            _changePasswordState.value = ChangePasswordState(
                false,
                ChangePasswordError.ReauthenticateFailedError
            )
            return@launch
        }

        hasRetried = true

        val reAuthSuccess = verifyCurrentPasswordUseCase(
            currentUser.email,
            _currentPassword.value
        )

        if (!reAuthSuccess) {
            hasRetried = false
            _changePasswordState.value = ChangePasswordState(
                false,
                ChangePasswordError.ReauthenticateFailedError
            )
            return@launch
        }

        // Retry update password
        updatePassword(currentUser)
    }
}