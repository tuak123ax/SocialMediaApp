package com.minhtu.firesocialmedia.presentation.loginhistory

import com.minhtu.firesocialmedia.core.domain.entity.settings.SessionItem

sealed class LoginHistoryUiState {
    object Loading : LoginHistoryUiState()
    data class Success(val data: List<SessionItem>) : LoginHistoryUiState()
    object Empty : LoginHistoryUiState()
    data class Error(val message: String) : LoginHistoryUiState()
}

