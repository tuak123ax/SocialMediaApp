package com.minhtu.firesocialmedia.presentation.loading

import com.rickclephas.kmp.observableviewmodel.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class SyncLoadingViewModel : ViewModel() {
    private val _isSyncing = MutableStateFlow(false)
    val isSyncing = _isSyncing.asStateFlow()

    fun showSyncLoading() {
        _isSyncing.value = true
    }
    fun hideSyncLoading() {
        _isSyncing.value = false
    }
}
