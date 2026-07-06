package com.minhtu.firesocialmedia.presentation.twofa

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.minhtu.firesocialmedia.core.domain.usecases.settings.CopyUseCase
import com.minhtu.firesocialmedia.core.domain.usecases.settings.UpdateVerify2FASuccessUseCase
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.launch

class TwoFactorEnabledViewModel(
    private val copyUseCase: CopyUseCase,
    private val updateVerify2FASuccessUseCase: UpdateVerify2FASuccessUseCase,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) : ViewModel() {
    fun copyToClipboard(backupCode: String) {
        viewModelScope.launch(ioDispatcher) {
            copyUseCase.invoke(backupCode)
        }
    }

    fun updateVerify2FASuccess() {
        viewModelScope.launch(ioDispatcher) {
            updateVerify2FASuccessUseCase.invoke()
        }
    }
}

