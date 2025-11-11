package com.minhtu.firesocialmedia.presentation.loading

import com.rickclephas.kmp.observableviewmodel.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class LoadingViewModel : ViewModel() {
    // StateFlow to hold the loading state
    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    fun showLoading() {
        _isLoading.value = true
    }
    fun hideLoading() {
        _isLoading.value = false
    }

    private val _syncLoading = MutableStateFlow(false)
    val syncLoading = _syncLoading.asStateFlow()

    fun showSyncLoading() {
        _syncLoading.value = true
    }
    fun hideSyncLoading() {
        _syncLoading.value = false
    }
}