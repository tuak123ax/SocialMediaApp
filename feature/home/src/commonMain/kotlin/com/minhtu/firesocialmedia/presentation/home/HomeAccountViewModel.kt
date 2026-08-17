package com.minhtu.firesocialmedia.presentation.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.minhtu.firesocialmedia.domain.usecases.home.ClearAccountUseCase
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class HomeAccountViewModel(
    private val clearAccountUseCase: ClearAccountUseCase,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) : ViewModel() {
    fun clearAccountInStorage() {
        viewModelScope.launch {
            withContext(ioDispatcher) {
                clearAccountUseCase.invoke()
            }
        }
    }
}
