package com.minhtu.firesocialmedia.presentation.navigationscreen.setting.group

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.minhtu.firesocialmedia.core.domain.entity.group.GroupInstance
import com.minhtu.firesocialmedia.core.domain.entity.user.UserInstance
import com.minhtu.firesocialmedia.core.domain.usecases.common.GetUserUseCase
import com.minhtu.firesocialmedia.core.domain.usecases.group.DemoteMemberUseCase
import com.minhtu.firesocialmedia.core.domain.usecases.group.PromoteMemberUseCase
import com.minhtu.firesocialmedia.core.domain.usecases.group.RemoveMemberUseCase
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ManageMembersViewModel(
    private val getUserUseCase : GetUserUseCase,
    private val removeMemberUseCase : RemoveMemberUseCase,
    private val promoteMemberUseCase : PromoteMemberUseCase,
    private val demoteMemberUseCase : DemoteMemberUseCase,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) : ViewModel() {
    suspend fun findUserById(userId: String) : UserInstance? {
        return getUserUseCase.invoke(userId, false)
    }

    private val _removeMemberStatus = MutableStateFlow<Boolean?>(null)
    var removeMemberStatus = _removeMemberStatus.asStateFlow()
    fun removeMember(member : UserInstance,
                     group : GroupInstance) {
        viewModelScope.launch(ioDispatcher) {
            _removeMemberStatus.value = removeMemberUseCase.invoke(member, group)
            group.members.remove(member.uid)
            member.groups.remove(group.id)
        }
    }
    fun resetRemoveMemberStatus() {
        _removeMemberStatus.value = null
    }

    private val _promoteMemberStatus = MutableStateFlow<Boolean?>(null)
    var promoteMemberStatus = _promoteMemberStatus.asStateFlow()
    fun promoteMember(member: UserInstance, group: GroupInstance) {
        viewModelScope.launch(ioDispatcher) {
            _promoteMemberStatus.value = promoteMemberUseCase.invoke(
                member,
                group)
            group.members[member.uid] = "admin"
        }
    }
    fun resetPromoteMemberStatus() {
        _promoteMemberStatus.value = null
    }
    private val _demoteMemberStatus = MutableStateFlow<Boolean?>(null)
    var demoteMemberStatus = _demoteMemberStatus.asStateFlow()
    fun demoteMember(member: UserInstance, group: GroupInstance) {
        viewModelScope.launch(ioDispatcher) {
            _demoteMemberStatus.value = demoteMemberUseCase.invoke(
                member,
                group)
            group.members[member.uid] = "member"
        }
    }
    fun resetDemoteMemberStatus() {
        _demoteMemberStatus.value = null
    }

    private val _fetchAdminListStatus = MutableStateFlow<List<UserInstance>>(emptyList())
    var fetchAdminListStatus = _fetchAdminListStatus.asStateFlow()
    fun fetchAdminList(adminSet : Set<String>) {
        viewModelScope.launch(ioDispatcher) {
            _fetchAdminListStatus.value = adminSet.map{ userId ->
                async {
                    findUserById(userId)
                }
            }.awaitAll().filterNotNull()
        }
    }
    fun addAdminToList(admin : UserInstance) {
        _fetchAdminListStatus.value += admin
    }

    fun removeAdminFromList(admin : UserInstance) {
        _fetchAdminListStatus.value -= admin
    }

    private val _fetchMemberListStatus = MutableStateFlow<List<UserInstance>>(emptyList())
    var fetchMemberListStatus = _fetchMemberListStatus.asStateFlow()
    fun fetchMemberList(memberSet: Set<String>) {
        viewModelScope.launch(ioDispatcher) {
            _fetchMemberListStatus.value = memberSet.map{ userId ->
                async {
                    findUserById(userId)
                }
            }.awaitAll().filterNotNull()
        }
    }

    fun addMemberToList(member : UserInstance) {
        _fetchMemberListStatus.value += member
    }

    fun removeMemberFromList(member : UserInstance) {
        _fetchMemberListStatus.value -= member
    }

    fun resetAdminAndMemberList() {
        _fetchAdminListStatus.value = emptyList()
        _fetchMemberListStatus.value = emptyList()
    }
}