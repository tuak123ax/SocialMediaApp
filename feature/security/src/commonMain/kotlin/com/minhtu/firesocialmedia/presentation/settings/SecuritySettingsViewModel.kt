package com.minhtu.firesocialmedia.presentation.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.minhtu.firesocialmedia.domain.entity.authentication.TwoFAResponse
import com.minhtu.firesocialmedia.security.data.remote.dto.user.UserDTO
import com.minhtu.firesocialmedia.security.entity.user.toSecurityUser
import com.minhtu.firesocialmedia.domain.usecases.settings.Disable2FAUseCase
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class SecuritySettingsViewModel(
    private val disable2FAUseCase: Disable2FAUseCase,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) : ViewModel() {
    private var _disable2FAStatus = MutableStateFlow<TwoFAResponse?>(null)
    var disable2FAStatus = _disable2FAStatus.asStateFlow()

    fun disable2FA(currentUser: UserDTO) {
        viewModelScope.launch(ioDispatcher) {
            _disable2FAStatus.value = disable2FAUseCase.invoke(currentUser.toSecurityUser())
        }
    }

    fun resetDisable2FAStatus() {
        _disable2FAStatus.value = null
    }
}

