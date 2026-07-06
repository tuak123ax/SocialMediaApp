package com.minhtu.firesocialmedia.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.Modifier
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import com.minhtu.firesocialmedia.core.constants.UiConstants
import com.minhtu.firesocialmedia.core.domain.signin.GoogleSignInHandler
import com.minhtu.firesocialmedia.di.PlatformContext
import com.minhtu.firesocialmedia.presentation.forgotpassword.ForgotPassword
import com.minhtu.firesocialmedia.presentation.information.Information
import com.minhtu.firesocialmedia.presentation.information.InformationViewModel
import com.minhtu.firesocialmedia.presentation.signin.SignIn
import com.minhtu.firesocialmedia.presentation.signin.SignInViewModel
import com.minhtu.firesocialmedia.presentation.signup.SignUp
import com.minhtu.firesocialmedia.platform.rememberPlatformImagePicker
import com.minhtu.firesocialmedia.presentation.loading.LoadingViewModel
import com.minhtu.firesocialmedia.presentation.navigation.AuthNavGraph
import com.minhtu.firesocialmedia.presentation.navigation.RouterViewModel
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel

class AuthNavGraphImpl : AuthNavGraph {

    override fun getInformationRoute(): String = Information.getScreenName()

    override fun registerRoutes(
        navGraphBuilder: NavGraphBuilder,
        navController: NavHostController,
        loadingViewModel: LoadingViewModel,
        routerViewModel: RouterViewModel,
        context: Any,
        platformContext: PlatformContext,
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

        navGraphBuilder.composable(route = Information.getScreenName()) {
            val informationViewModel: InformationViewModel = koinViewModel()
            val picker = rememberPlatformImagePicker(
                context = context,
                onImagePicked = { uri -> informationViewModel.updateAvatar(uri) },
                onVideoPicked = {}
            )
            Information.InformationScreen(
                platform = platformContext,
                imagePicker = picker,
                signUpEmail = informationViewModel.pendingSignUpEmail,
                signUpPassword = informationViewModel.pendingSignUpPassword,
                informationViewModel = informationViewModel,
                loadingViewModel = loadingViewModel,
                onNavigateToHomeScreen = onNavigateToHome
            )
        }
    }
}




