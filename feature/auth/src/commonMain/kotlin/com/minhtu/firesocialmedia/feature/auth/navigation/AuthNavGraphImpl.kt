package com.minhtu.firesocialmedia.feature.auth.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.Modifier
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import com.minhtu.firesocialmedia.core.constants.UiConstants
import com.minhtu.firesocialmedia.core.domain.signin.GoogleSignInHandler
import com.minhtu.firesocialmedia.feature.auth.presentation.forgotpassword.ForgotPassword
import com.minhtu.firesocialmedia.feature.auth.presentation.signin.SignIn
import com.minhtu.firesocialmedia.feature.auth.presentation.signin.SignInViewModel
import com.minhtu.firesocialmedia.feature.auth.presentation.signup.SignUp
import com.minhtu.firesocialmedia.presentation.loading.LoadingViewModel
import com.minhtu.firesocialmedia.presentation.navigation.AuthNavGraph
import com.minhtu.firesocialmedia.presentation.navigation.RouterViewModel
import org.koin.compose.koinInject

class AuthNavGraphImpl : AuthNavGraph {
    override fun registerRoutes(
        navGraphBuilder: NavGraphBuilder,
        navController: NavHostController,
        loadingViewModel: LoadingViewModel,
        routerViewModel: RouterViewModel,
        onNavigateToHome: () -> Unit,
        onNavigateToInformation: () -> Unit,
        onNavigateToVerifyOTP: () -> Unit
    ) {
        navGraphBuilder.composable(route = UiConstants.SignIn.SCREEN_NAME) {
            val signInViewModel = koinInject<GoogleSignInHandler>() as SignInViewModel
            SignIn.SignInScreen(
                signInViewModel = signInViewModel,
                loadingViewModel = loadingViewModel,
                routerViewModel = routerViewModel,
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.background),
                onNavigateToSignUpScreen = {
                    navController.navigate(route = UiConstants.SignUp.SCREEN_NAME)
                },
                onNavigateToHomeScreen = onNavigateToHome,
                onNavigateToInformationScreen = onNavigateToInformation,
                onNavigateToForgotPasswordScreen = {
                    navController.navigate(route = UiConstants.ForgotPassword.SCREEN_NAME)
                },
                onNavigateToVerifyOTP = onNavigateToVerifyOTP
            )
        }

        navGraphBuilder.composable(route = UiConstants.SignUp.SCREEN_NAME) {
            SignUp.SignUpScreen(
                loadingViewModel = loadingViewModel,
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.background),
                onNavigateToSignInScreen = {
                    navController.navigate(UiConstants.SignIn.SCREEN_NAME) {
                        popUpTo(0) { inclusive = true }
                        launchSingleTop = true
                    }
                },
                onNavigateToInformationScreen = onNavigateToInformation
            )
        }

        navGraphBuilder.composable(route = UiConstants.ForgotPassword.SCREEN_NAME) {
            ForgotPassword.ForgotPasswordScreen(
                loadingViewModel = loadingViewModel,
                onNavigateToSignInScreen = {
                    navController.navigate(UiConstants.SignIn.SCREEN_NAME) {
                        popUpTo(0) { inclusive = true }
                        launchSingleTop = true
                    }
                }
            )
        }
    }
}




