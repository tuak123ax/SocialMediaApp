package com.minhtu.firesocialmedia.presentation.selectgroup

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.minhtu.firesocialmedia.core.domain.entity.group.GroupInstance
import com.minhtu.firesocialmedia.core.domain.usecases.group.GetAllGroupsUseCase
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class SelectGroupViewModel(
    private val getAllGroupsUseCase : GetAllGroupsUseCase,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) : ViewModel() {
    private var _getAllGroupsState = MutableStateFlow<Set<GroupInstance>?>(null)
    var getAllGroupsState = _getAllGroupsState.asStateFlow()
    fun getAllGroupsOfUser(userId : String) {
        viewModelScope.launch {
            withContext(ioDispatcher) {
                _getAllGroupsState.value = getAllGroupsUseCase.invoke(userId)
            }
        }
    }
}