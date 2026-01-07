package com.minhtu.firesocialmedia.presentation.navigationscreen.setting.group

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.minhtu.firesocialmedia.constants.Constants
import com.minhtu.firesocialmedia.domain.core.DecentralizationType
import com.minhtu.firesocialmedia.domain.entity.group.GroupInstance
import com.minhtu.firesocialmedia.domain.entity.user.UserInstance
import com.minhtu.firesocialmedia.domain.usecases.group.CreateGroupUseCase
import com.minhtu.firesocialmedia.platform.generateRandomId
import com.minhtu.firesocialmedia.platform.getCurrentTime
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class CreateGroupViewModel(
    private val createGroupUseCase: CreateGroupUseCase,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) : ViewModel() {
    private val _groupName = MutableStateFlow("")
    var groupName = _groupName.asStateFlow()

    fun updateGroupName(name : String) {
        _groupName.value = name
    }

    var password = MutableStateFlow("")
    fun updatePassword(input : String){
        password.value =  input
    }

    var avatar by mutableStateOf(Constants.DEFAULT_AVATAR_URL)
    fun updateAvatar(newAvatar : String) {
        avatar = newAvatar
    }
    private val _accessPermission = MutableStateFlow<DecentralizationType>(DecentralizationType.Public)
    var accessPermission = _accessPermission.asStateFlow()
    fun updateAccessPermission(permission : DecentralizationType) {
        _accessPermission.value = permission
    }
    fun resetAccessPermission() {
        _accessPermission.value = DecentralizationType.Public
    }

    private val _createGroupState = MutableStateFlow<GroupInstance?>(null)
    var createGroupState = _createGroupState.asStateFlow()
    fun resetCreateGroupState() {
        _createGroupState.value = null
    }
    fun createGroup(currentUser : UserInstance) {
        viewModelScope.launch(ioDispatcher) {
            val memberMap = HashMap<String, String>()
            //Who create group will be the admin and the first member
            memberMap[currentUser.uid] = "admin"
            val groupInstance = GroupInstance(
                id = generateGroupId(),
                name = _groupName.value,
                avatar = avatar,
                password = if(_accessPermission.value == DecentralizationType.Private) password.value else "",
                createdDate = getCurrentTime(),
                members = memberMap,
                posts = HashMap()
            )
            currentUser.groups[groupInstance.id] = groupInstance
            val result = createGroupUseCase.invoke(
                groupInstance,
                currentUser.uid)
            if(result) {
                _createGroupState.value = groupInstance
            } else {
                currentUser.groups.remove(groupInstance.id)
                _createGroupState.value = GroupInstance()
            }
            resetAccessPermission()
        }
    }

    private fun generateGroupId(): String {
        return "group-"+ generateRandomId()
    }
}