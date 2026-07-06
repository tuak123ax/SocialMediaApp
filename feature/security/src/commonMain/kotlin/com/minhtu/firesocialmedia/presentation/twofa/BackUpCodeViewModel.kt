package com.minhtu.firesocialmedia.presentation.twofa

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.minhtu.firesocialmedia.core.domain.entity.authentication.TwoFAResponse
import com.minhtu.firesocialmedia.core.domain.usecases.common.GetCurrentUserUidUseCase
import com.minhtu.firesocialmedia.core.domain.usecases.settings.VerifyBackupCodeUseCase
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class BackUpCodeViewModel(
    private val getCurrentUserUidUseCase: GetCurrentUserUidUseCase,
    private val verifyBackupCodeUseCase: VerifyBackupCodeUseCase,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) : ViewModel() {
    private var _backupCode = MutableStateFlow("")
    var backupCode = _backupCode.asStateFlow()

    fun updateBackupCode(input: String) {
        _backupCode.value = input
    }

    private val _verifyBackupCodeStatus = MutableStateFlow<TwoFAResponse?>(null)
    val verifyBackupCodeStatus = _verifyBackupCodeStatus.asStateFlow()

    fun verifyBackupCode(backupCode: String) {
        viewModelScope.launch(ioDispatcher) {
            val userId = getCurrentUserUidUseCase.invoke()
            if (userId != null) {
                _verifyBackupCodeStatus.value = verifyBackupCodeUseCase.invoke(userId, backupCode)
            }
        }
    }

    fun resetVerifyBackupCodeStatus() {
        _verifyBackupCodeStatus.value = null
    }
}

