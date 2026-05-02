package com.minhtu.firesocialmedia.presentation.navigationscreen.setting.security.twoFA

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.minhtu.firesocialmedia.domain.usecases.settings.BuildOtpAuthUrlUseCase
import com.minhtu.firesocialmedia.domain.usecases.settings.CopyUseCase
import com.minhtu.firesocialmedia.domain.usecases.settings.GenerateSecretFor2FAUseCase
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class TwoFAViewModel(
    private val generateSecretFor2FAUseCase : GenerateSecretFor2FAUseCase,
    private val buildOtpAuthUrlUseCase : BuildOtpAuthUrlUseCase,
    private val copySecretUseCase : CopyUseCase,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) : ViewModel(){
    private val _secretFor2FA = MutableStateFlow<String?>(null)
    val secretFor2FA = _secretFor2FA.asStateFlow()
    fun generateSecretFor2FA() {
        viewModelScope.launch(ioDispatcher) {
            _secretFor2FA.value = generateSecretFor2FAUseCase.invoke()
        }
    }

    fun buildOtpAuthUrl(appName: String,
                        user: String,
                        secret: String?) : String{
        return buildOtpAuthUrlUseCase.invoke(
            appName,
            user,
            secret
        )
    }

    fun copySecret(secret : String) {
        viewModelScope.launch(ioDispatcher) {
            copySecretUseCase.invoke(secret)
        }
    }

}