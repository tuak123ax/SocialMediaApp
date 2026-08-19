package com.minhtu.firesocialmedia.presentation.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import com.minhtu.firesocialmedia.search.entity.user.UserInstance
import com.minhtu.firesocialmedia.di.PlatformContext

interface AuthNavGraph {
    fun getInformationRoute(): String

    fun registerRoutes(
        navGraphBuilder: NavGraphBuilder,
        navController: NavHostController,
        onUserSignedIn: (UserInstance) -> Unit,
        context: Any,
        platformContext: PlatformContext,
        onNavigateToHome: () -> Unit,
        onNavigateToInformation: () -> Unit,
        onNavigateToVerifyOTP: () -> Unit
    )
}