package com.minhtu.firesocialmedia.presentation.navigationscreen.setting.security.loginhistory

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.minhtu.firesocialmedia.data.remote.constant.DataConstant
import com.minhtu.firesocialmedia.core.domain.entity.user.UserInstance
import com.minhtu.firesocialmedia.core.domain.usecases.settings.DeleteLoginSessionUseCase
import com.minhtu.firesocialmedia.core.domain.usecases.settings.FetchLoginHistoryListUseCase
import com.minhtu.firesocialmedia.core.domain.usecases.settings.LogoutSessionUseCase
import com.minhtu.firesocialmedia.core.domain.usecases.settings.UpdateUserTimestampUseCase
import com.minhtu.firesocialmedia.core.domain.usecases.settings.VerifyCurrentPasswordUseCase
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlin.time.Clock.System
import kotlin.time.ExperimentalTime

enum class AcknowledgeStatus { IDLE, LOADING, SUCCESS, ERROR }
enum class LogoutSessionStatus { IDLE, WRONG_PASSWORD, ERROR }

class LoginHistoryViewModel(
    private val fetchLoginHistoryListUseCase : FetchLoginHistoryListUseCase,
    private val updateUserTimestampUseCase: UpdateUserTimestampUseCase,
    private val deleteLoginSessionUseCase: DeleteLoginSessionUseCase,
    private val logoutSessionUseCase: LogoutSessionUseCase,
    private val verifyCurrentPasswordUseCase: VerifyCurrentPasswordUseCase,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) : ViewModel() {
    private val _loginHistoryUiState = MutableStateFlow<LoginHistoryUiState>(LoginHistoryUiState.Loading)
    val loginHistoryUiState = _loginHistoryUiState.asStateFlow()

    private val _acknowledgeStatus = MutableStateFlow(AcknowledgeStatus.IDLE)
    val acknowledgeStatus = _acknowledgeStatus.asStateFlow()

    private val _logoutSessionStatus = MutableStateFlow(LogoutSessionStatus.IDLE)
    val logoutSessionStatus = _logoutSessionStatus.asStateFlow()

    fun resetAcknowledgeStatus() {
        _acknowledgeStatus.value = AcknowledgeStatus.IDLE
    }

    fun resetLogoutSessionStatus() {
        _logoutSessionStatus.value = LogoutSessionStatus.IDLE
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

    fun deleteLoginSession(userId: String, email: String, password: String, sessionId: String) {
        viewModelScope.launch(ioDispatcher) {
            val authenticated = runCatching {
                verifyCurrentPasswordUseCase.invoke(email, password)
            }.getOrElse { false }

            if (!authenticated) {
                _logoutSessionStatus.value = LogoutSessionStatus.WRONG_PASSWORD
                return@launch
            }

            val success = runCatching {
                deleteLoginSessionUseCase.invoke(userId, sessionId)
            }.getOrElse { false }
            if (success) {
                fetchLoginHistoryList(userId)
            } else {
                _logoutSessionStatus.value = LogoutSessionStatus.ERROR
            }
        }
    }

    fun logoutSession(userId: String, email: String, password: String, sessionId: String) {
        viewModelScope.launch(ioDispatcher) {
            val authenticated = runCatching {
                verifyCurrentPasswordUseCase.invoke(email, password)
            }.getOrElse { false }

            if (!authenticated) {
                _logoutSessionStatus.value = LogoutSessionStatus.WRONG_PASSWORD
                return@launch
            }

            val success = runCatching {
                logoutSessionUseCase.invoke(userId, sessionId)
            }.getOrElse { false }

            if (success) {
                fetchLoginHistoryList(userId)
            } else {
                _logoutSessionStatus.value = LogoutSessionStatus.ERROR
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