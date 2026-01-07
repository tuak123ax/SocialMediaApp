package com.minhtu.firesocialmedia.presentation.navigationscreen.setting.group

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.minhtu.firesocialmedia.constants.Constants
import com.minhtu.firesocialmedia.domain.entity.group.GroupInstance
import com.minhtu.firesocialmedia.domain.usecases.group.FetchGroupInfoUseCase
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class GroupDetailsViewModel(
    private val fetchGroupInfoUseCase : FetchGroupInfoUseCase,
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
}