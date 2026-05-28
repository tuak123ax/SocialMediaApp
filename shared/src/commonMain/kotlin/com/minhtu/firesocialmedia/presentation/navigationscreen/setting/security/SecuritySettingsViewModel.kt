package com.minhtu.firesocialmedia.presentation.navigationscreen.setting.security

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.minhtu.firesocialmedia.core.domain.entity.authentication.TwoFAResponse
import com.minhtu.firesocialmedia.core.domain.entity.user.UserInstance
import com.minhtu.firesocialmedia.core.domain.usecases.settings.Disable2FAUseCase
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class SecuritySettingsViewModel(
    private val disable2FAUseCase : Disable2FAUseCase,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) : ViewModel() {
    private var _disable2FAStatus = MutableStateFlow<TwoFAResponse?>(null)
    var disable2FAStatus = _disable2FAStatus.asStateFlow()
    fun disable2FA(currentUser : UserInstance) {
        viewModelScope.launch(ioDispatcher) {
            _disable2FAStatus.value = disable2FAUseCase.invoke(currentUser)
        }
    }

    fun resetDisable2FAStatus() {
        _disable2FAStatus.value = null
    }
}