package com.minhtu.firesocialmedia.presentation.navigationscreen.setting.security.loginhistory

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.minhtu.firesocialmedia.data.remote.constant.DataConstant
import com.minhtu.firesocialmedia.domain.entity.user.UserInstance
import com.minhtu.firesocialmedia.domain.usecases.settings.FetchLoginHistoryListUseCase
import com.minhtu.firesocialmedia.domain.usecases.settings.UpdateUserTimestampUseCase
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlin.time.Clock.System
import kotlin.time.ExperimentalTime

enum class AcknowledgeStatus { IDLE, LOADING, SUCCESS, ERROR }

class LoginHistoryViewModel(
    private val fetchLoginHistoryListUseCase : FetchLoginHistoryListUseCase,
    private val updateUserTimestampUseCase: UpdateUserTimestampUseCase,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) : ViewModel() {
    private val _loginHistoryUiState = MutableStateFlow<LoginHistoryUiState>(LoginHistoryUiState.Loading)
    val loginHistoryUiState = _loginHistoryUiState.asStateFlow()

    private val _acknowledgeStatus = MutableStateFlow(AcknowledgeStatus.IDLE)
    val acknowledgeStatus = _acknowledgeStatus.asStateFlow()

    fun resetAcknowledgeStatus() {
        _acknowledgeStatus.value = AcknowledgeStatus.IDLE
    }

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

    @OptIn(ExperimentalTime::class)
    fun acknowledgeLoginHistory(user: UserInstance) {
        viewModelScope.launch(ioDispatcher) {
            _acknowledgeStatus.value = AcknowledgeStatus.LOADING
            val now = System.now().toEpochMilliseconds()
            val success = updateUserTimestampUseCase.invoke(
                user.uid,
                DataConstant.LAST_TIME_ACKNOWLEDGED_LOGIN_HISTORY_PATH,
                now
            )
            if (success) {
                user.lastTimeAcknowledgedLoginHistory = now
                _acknowledgeStatus.value = AcknowledgeStatus.SUCCESS
            } else {
                _acknowledgeStatus.value = AcknowledgeStatus.ERROR
            }
        }
    }

    @OptIn(ExperimentalTime::class)
    fun acknowledgePrivacyRead(user: UserInstance) {
        viewModelScope.launch(ioDispatcher) {
            val now = System.now().toEpochMilliseconds()
            val success = updateUserTimestampUseCase.invoke(
                user.uid,
                DataConstant.LAST_TIME_READ_PRIVACY_PATH,
                now
            )
            if (success) {
                user.lastTimeReadPrivacy = now
            }
        }
    }
}