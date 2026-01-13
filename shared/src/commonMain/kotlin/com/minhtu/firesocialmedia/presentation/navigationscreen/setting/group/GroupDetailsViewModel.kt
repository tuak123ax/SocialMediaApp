package com.minhtu.firesocialmedia.presentation.navigationscreen.setting.group

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.minhtu.firesocialmedia.constants.Constants
import com.minhtu.firesocialmedia.domain.entity.group.GroupInstance
import com.minhtu.firesocialmedia.domain.usecases.group.FetchGroupInfoUseCase
import com.minhtu.firesocialmedia.domain.usecases.group.FetchNotificationStateUseCase
import com.minhtu.firesocialmedia.domain.usecases.group.FindGroupByIdUseCase
import com.minhtu.firesocialmedia.domain.usecases.group.UpdateNotificationStatusUseCase
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class GroupDetailsViewModel(
    private val fetchGroupInfoUseCase : FetchGroupInfoUseCase,
    private val updateNotificationStatusUseCase : UpdateNotificationStatusUseCase,
    private val fetchNotificationStateUseCase : FetchNotificationStateUseCase,
    private val findGroupByIdUseCase : FindGroupByIdUseCase,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) : ViewModel() {
    var coverPhoto by mutableStateOf(Constants.DEFAULT_AVATAR_URL)
    fun updateCover(input:String){
        coverPhoto = input
    }

    private val _fetchGroupInfoState = MutableStateFlow<GroupInstance?>(null)
    var fetchGroupInfoState = _fetchGroupInfoState.asStateFlow()
    fun fetchGroupInfo(groupId : String) {
        viewModelScope.launch {
            withContext(ioDispatcher) {
                _fetchGroupInfoState.value = fetchGroupInfoUseCase.invoke(groupId)
            }
        }
    }

    private val _notificationState = MutableStateFlow<Boolean?>(null)
    var notificationState = _notificationState.asStateFlow()
    private val _updateNotificationState = MutableStateFlow<Boolean?>(null)
    var updateNotificationState = _updateNotificationState.asStateFlow()
    fun updateNotificationStateWhenClickButton() {
        if(_notificationState.value != null) {
            _notificationState.value = !_notificationState.value!!
        }
    }
    fun updateNotificationStatus(
        groupId : String,
        userId : String) {
        viewModelScope.launch {
            withContext(ioDispatcher) {
                updateNotificationStateWhenClickButton()
                if(_notificationState.value != null) {
                    _updateNotificationState.value = updateNotificationStatusUseCase.invoke(
                        _notificationState.value!!,
                        groupId,
                        userId)
                }
            }
        }
    }

    fun fetchNotificationState(userId: String, groupId: String) {
        viewModelScope.launch {
            withContext(ioDispatcher) {
                _notificationState.value = fetchNotificationStateUseCase.invoke(
                    userId,
                    groupId
                )
            }
        }
    }

    private val _groupDetailsFromDeepLink = MutableStateFlow<GroupInstance?>(null)
    var groupDetailsFromDeepLink = _groupDetailsFromDeepLink.asStateFlow()
    suspend fun requestFindGroupDetailsById(groupId: String) {
        _groupDetailsFromDeepLink.value = findGroupByIdUseCase.invoke(groupId)
    }
}