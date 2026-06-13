package com.minhtu.firesocialmedia.presentation.navigation

import androidx.compose.runtime.ProvidedValue
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import com.minhtu.firesocialmedia.presentation.loading.LoadingViewModel

interface SecurityNavGraph {
    fun getSecuritySettingsRoute(): String
    fun getPrivacyRoute(): String
    fun registerRoutes(
        navGraphBuilder: NavGraphBuilder,
        navController: NavHostController,
        loadingViewModel: LoadingViewModel,
        localImageLoaderValue: ProvidedValue<*>,
        getCurrentUser: () -> com.minhtu.firesocialmedia.core.domain.entity.user.UserInstance?,
        getHomeRoute: () -> String,
        onNavigateToForgotPassword: () -> Unit,
        onNavigateToSignIn: () -> Unit
    )
}


