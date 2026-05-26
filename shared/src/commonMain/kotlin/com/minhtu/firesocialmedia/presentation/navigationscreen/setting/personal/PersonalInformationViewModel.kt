package com.minhtu.firesocialmedia.presentation.navigationscreen.setting.personal

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.minhtu.firesocialmedia.data.remote.constant.DataConstant
import com.minhtu.firesocialmedia.domain.entity.user.UserInstance
import com.minhtu.firesocialmedia.domain.usecases.common.GetUserUseCase
import com.minhtu.firesocialmedia.domain.usecases.settings.UpdateUserAvatarUseCase
import com.minhtu.firesocialmedia.domain.usecases.settings.UpdateUserStringFieldUseCase
import com.minhtu.firesocialmedia.domain.usecases.settings.VerifyCurrentPasswordUseCase
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class PersonalInformationViewModel(
    private val updateUserStringFieldUseCase: UpdateUserStringFieldUseCase,
    private val updateUserAvatarUseCase: UpdateUserAvatarUseCase,
    private val verifyCurrentPasswordUseCase: VerifyCurrentPasswordUseCase,
    private val getUserUseCase: GetUserUseCase,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) : ViewModel() {

    private val _updateStatus = MutableStateFlow<Boolean?>(null)
    val updateStatus = _updateStatus.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    // null=idle, true=auth ok & saved, false=wrong password
    private val _reAuthStatus = MutableStateFlow<Boolean?>(null)
    val reAuthStatus = _reAuthStatus.asStateFlow()

    // Holds the freshly fetched user; null means fetch not yet done or failed
    private val _fetchedUser = MutableStateFlow<UserInstance?>(null)
    val fetchedUser = _fetchedUser.asStateFlow()

    // Holds the local URI of a newly picked avatar (prior to upload)
    var avatarUri by mutableStateOf<String?>(null)
        private set

    // Holds the remote path of the successfully uploaded avatar, to update UI without refetching
    var uploadedAvatarUri by mutableStateOf<String?>(null)
        private set

    fun fetchCurrentUser(userId: String) {
        viewModelScope.launch(ioDispatcher) {
            val user = getUserUseCase.invoke(userId, isCurrentUser = true)
            if (user != null) {
                _fetchedUser.value = user
            }
            // if null (network error etc.), _fetchedUser stays null → UI falls back to passed-in currentUser
        }
    }

    fun onAvatarPicked(uri: String) {
        avatarUri = uri
    }

    fun updateAvatar(userId: String) {
        val uri = avatarUri ?: return
        viewModelScope.launch(ioDispatcher) {
            _isLoading.value = true
            val result = updateUserAvatarUseCase(userId, uri)
            _updateStatus.value = result
            if (result) {
                uploadedAvatarUri = uri // keep local URI so UI shows new avatar immediately
                avatarUri = null        // dismiss the save button
            }
            _isLoading.value = false
        }
    }

    fun reAuthAndUpdatePhone(email: String, password: String, userId: String, phone: String) {
        viewModelScope.launch(ioDispatcher) {
            _isLoading.value = true
            val authenticated = verifyCurrentPasswordUseCase(email, password)
            if (authenticated) {
                val result = updateUserStringFieldUseCase(userId, DataConstant.PHONE_PATH, phone)
                _updateStatus.value = result
                _reAuthStatus.value = if (result) null else false
            } else {
                _reAuthStatus.value = false
            }
            _isLoading.value = false
        }
    }

    fun resetReAuthStatus() {
        _reAuthStatus.value = null
    }

    fun updateName(userId: String, name: String) {
        viewModelScope.launch(ioDispatcher) {
            _isLoading.value = true
            val result = updateUserStringFieldUseCase(userId, DataConstant.NAME_PATH, name)
            _updateStatus.value = result
            _isLoading.value = false
        }
    }

    fun updateStatus(userId: String, status: String) {
        viewModelScope.launch(ioDispatcher) {
            _isLoading.value = true
            val result = updateUserStringFieldUseCase(userId, DataConstant.STATUS_PATH, status)
            _updateStatus.value = result
            _isLoading.value = false
        }
    }

    fun resetUpdateStatus() {
        _updateStatus.value = null
    }
}