package com.minhtu.firesocialmedia.presentation.navigationscreen.setting.security.loginhistory

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.minhtu.firesocialmedia.domain.usecases.settings.FetchLoginHistoryListUseCase
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class LoginHistoryViewModel(
    private val fetchLoginHistoryListUseCase : FetchLoginHistoryListUseCase,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) : ViewModel() {
    private val _loginHistoryUiState = MutableStateFlow<LoginHistoryUiState>(LoginHistoryUiState.Loading)
    val loginHistoryUiState = _loginHistoryUiState.asStateFlow()
    fun fetchLoginHistoryList(userId : String) {
        viewModelScope.launch(ioDispatcher) {
            _loginHistoryUiState.value = LoginHistoryUiState.Loading

            runCatching {
                fetchLoginHistoryListUseCase.invoke(userId)
            }.onSuccess { list ->
                _loginHistoryUiState.value = if (list.isEmpty()) {
                    LoginHistoryUiState.Empty
                } else {
                    LoginHistoryUiState.Success(list)
                }
            }.onFailure {
                _loginHistoryUiState.value = LoginHistoryUiState.Error(it.message ?: "Unknown error")
            }
        }
    }
}