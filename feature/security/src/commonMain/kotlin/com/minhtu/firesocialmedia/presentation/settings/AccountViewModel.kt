package com.minhtu.firesocialmedia.presentation.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.minhtu.firesocialmedia.domain.usecases.home.security.ClearLocalDataUseCase
import com.minhtu.firesocialmedia.domain.usecases.home.security.ClearAccountUseCase
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class AccountViewModel(
    private val clearAccountUseCase: ClearAccountUseCase,
    private val clearLocalDataUseCase: ClearLocalDataUseCase,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) : ViewModel() {
    fun clearAccountInStorage() {
        viewModelScope.launch {
            withContext(ioDispatcher) {
                clearAccountUseCase.invoke()
            }
        }
    }

    fun clearLocalData() {
        viewModelScope.launch {
            withContext(ioDispatcher) {
                clearLocalDataUseCase.invoke()
            }
        }
    }
}
