package com.minhtu.firesocialmedia.presentation.navigationscreen.setting.group

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.minhtu.firesocialmedia.domain.usecases.group.CopyLinkUseCase
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.launch

class InviteMemberViewModel(
    private val copyLinkUseCase : CopyLinkUseCase,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) : ViewModel() {
    fun copyLink(copyData : String) {
        viewModelScope.launch(ioDispatcher) {
            copyLinkUseCase.invoke(copyData)
        }
    }
}