package com.minhtu.firesocialmedia.loading

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class LoadingViewModel : ViewModel() {
    // StateFlow to hold the loading state
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    fun showLoading() {
        _isLoading.value = true
    }
    fun hideLoading() {
        _isLoading.value = false
    }
}