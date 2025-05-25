package com.minhtu.firesocialmedia.loading

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
}