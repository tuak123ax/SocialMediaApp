package com.minhtu.firesocialmedia.core.utils

interface NavigationHandler {
    fun navigateTo(route: String)
    fun navigateBack()
    fun getCurrentRoute(): String?
}

