package com.minhtu.firesocialmedia.presentation.navigation

import androidx.compose.runtime.ProvidedValue
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import com.minhtu.firesocialmedia.search.entity.user.UserInstance

interface SecurityNavGraph {
    fun getSecuritySettingsRoute(): String
    fun getPrivacyRoute(): String
    fun registerRoutes(
        navGraphBuilder: NavGraphBuilder,
        navController: NavHostController,
        localImageLoaderValue: ProvidedValue<*>,
        getCurrentUser: () -> UserInstance?,
        getHomeRoute: () -> String,
        onNavigateToForgotPassword: () -> Unit,
        onNavigateToSignIn: () -> Unit
    )
}


