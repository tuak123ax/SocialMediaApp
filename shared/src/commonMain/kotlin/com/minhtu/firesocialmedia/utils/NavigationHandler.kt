package com.minhtu.firesocialmedia.utils

interface NavigationHandler {
    fun navigateTo(route: String)
    fun navigateBack()
    fun getCurrentRoute() : String?
}