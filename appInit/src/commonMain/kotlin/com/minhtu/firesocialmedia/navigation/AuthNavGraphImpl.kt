package com.minhtu.firesocialmedia.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.Modifier
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import com.minhtu.firesocialmedia.constants.AuthRouteNames
import com.minhtu.firesocialmedia.domain.signin.GoogleSignInHandler
import com.minhtu.firesocialmedia.domain.entity.user.auth.toDto
import com.minhtu.firesocialmedia.entity.bridge.toAppInitUserDto
import com.minhtu.firesocialmedia.search.entity.user.UserInstance
import com.minhtu.firesocialmedia.search.entity.user.toSearchUser
import com.minhtu.firesocialmedia.di.PlatformContext
import com.minhtu.firesocialmedia.presentation.forgotpassword.ForgotPassword
import com.minhtu.firesocialmedia.presentation.information.Information
import com.minhtu.firesocialmedia.presentation.information.InformationViewModel
import com.minhtu.firesocialmedia.presentation.signin.SignIn
import com.minhtu.firesocialmedia.presentation.signin.SignInViewModel
import com.minhtu.firesocialmedia.presentation.signup.SignUp
import com.minhtu.firesocialmedia.data.remote.auth.service.imagepicker.auth.rememberPlatformImagePicker
import com.minhtu.firesocialmedia.presentation.navigation.AuthNavGraph
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel

class AuthNavGraphImpl : AuthNavGraph {

    override fun getInformationRoute(): String = Information.getScreenName()

    override fun registerRoutes(
        navGraphBuilder: NavGraphBuilder,
        navController: NavHostController,
        onUserSignedIn: (UserInstance) -> Unit,
        context: Any,
        platformContext: PlatformContext,
        onNavigateToHome: () -> Unit,
        onNavigateToInformation: () -> Unit,
        onNavigateToVerifyOTP: () -> Unit
    ) {
        navGraphBuilder.composable(route = AuthRouteNames.SignIn.SCREEN_NAME) {
            val signInViewModel = koinInject<GoogleSignInHandler>() as SignInViewModel
            SignIn.SignInScreen(
                signInViewModel = signInViewModel,
                onUserSignedIn = { authUser -> onUserSignedIn(authUser.toDto().toAppInitUserDto().toSearchUser()) },
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.background),
                onNavigateToSignUpScreen = {
                    navController.navigate(route = AuthRouteNames.SignUp.SCREEN_NAME)
                },
                onNavigateToHomeScreen = onNavigateToHome,
                onNavigateToInformationScreen = onNavigateToInformation,
                onNavigateToForgotPasswordScreen = {
                    navController.navigate(route = AuthRouteNames.ForgotPassword.SCREEN_NAME)
                },
                onNavigateToVerifyOTP = onNavigateToVerifyOTP
            )
        }

        navGraphBuilder.composable(route = AuthRouteNames.SignUp.SCREEN_NAME) {
            SignUp.SignUpScreen(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.background),
                onNavigateToSignInScreen = {
                    navController.navigate(AuthRouteNames.SignIn.SCREEN_NAME) {
                        popUpTo(0) { inclusive = true }
                        launchSingleTop = true
                    }
                },
                onNavigateToInformationScreen = onNavigateToInformation
            )
        }

        navGraphBuilder.composable(route = AuthRouteNames.ForgotPassword.SCREEN_NAME) {
            ForgotPassword.ForgotPasswordScreen(
                onNavigateToSignInScreen = {
                    navController.navigate(AuthRouteNames.SignIn.SCREEN_NAME) {
                        popUpTo(0) { inclusive = true }
                        launchSingleTop = true
                    }
                }
            )
        }

        navGraphBuilder.composable(route = Information.getScreenName()) {
            val informationViewModel: InformationViewModel = koinViewModel()
            val picker =
                _root_ide_package_.com.minhtu.firesocialmedia.data.remote.auth.service.imagepicker.auth.rememberPlatformImagePicker(
                    context = context,
                    onImagePicked = { uri -> informationViewModel.updateAvatar(uri) }
                )
            Information.InformationScreen(
                platform = platformContext,
                imagePicker = picker,
                signUpEmail = informationViewModel.pendingSignUpEmail,
                signUpPassword = informationViewModel.pendingSignUpPassword,
                informationViewModel = informationViewModel,
                onNavigateToHomeScreen = onNavigateToHome
            )
        }
    }
}




