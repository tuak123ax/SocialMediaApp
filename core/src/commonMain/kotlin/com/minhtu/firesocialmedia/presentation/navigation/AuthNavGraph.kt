package com.minhtu.firesocialmedia.presentation.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import com.minhtu.firesocialmedia.di.PlatformContext
import com.minhtu.firesocialmedia.presentation.loading.LoadingViewModel

interface AuthNavGraph {
    fun getInformationRoute(): String

    fun registerRoutes(
        navGraphBuilder: NavGraphBuilder,
        navController: NavHostController,
        loadingViewModel: LoadingViewModel,
        routerViewModel: RouterViewModel,
        context: Any,
        platformContext: PlatformContext,
        onNavigateToHome: () -> Unit,
        onNavigateToInformation: () -> Unit,
        onNavigateToVerifyOTP: () -> Unit
    )
}