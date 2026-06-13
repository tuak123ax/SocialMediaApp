package com.minhtu.firesocialmedia.feature.security.navigation

import androidx.compose.runtime.ProvidedValue
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import com.minhtu.firesocialmedia.core.domain.entity.user.UserInstance
import com.minhtu.firesocialmedia.feature.security.presentation.changepassword.ChangePassword
import com.minhtu.firesocialmedia.feature.security.presentation.loginhistory.LoginHistory
import com.minhtu.firesocialmedia.feature.security.presentation.loginhistory.LoginHistoryViewModel
import com.minhtu.firesocialmedia.feature.security.presentation.settings.SecuritySettings
import com.minhtu.firesocialmedia.feature.security.presentation.twofa.BackUpCode
import com.minhtu.firesocialmedia.feature.security.presentation.twofa.TwoFA
import com.minhtu.firesocialmedia.feature.security.presentation.twofa.TwoFactorEnabled
import com.minhtu.firesocialmedia.feature.security.presentation.twofa.VerifyOTP
import com.minhtu.firesocialmedia.platform.showToast
import com.minhtu.firesocialmedia.presentation.loading.LoadingViewModel
import com.minhtu.firesocialmedia.presentation.navigation.DefaultNavAnimations
import com.minhtu.firesocialmedia.presentation.navigation.SecurityNavGraph
import com.minhtu.firesocialmedia.presentation.navigationscreen.setting.privacy.Privacy
import org.koin.compose.viewmodel.koinViewModel

class SecurityNavGraphImpl : SecurityNavGraph {

    override fun getSecuritySettingsRoute(): String = SecuritySettings.getScreenName()

    override fun getPrivacyRoute(): String = Privacy.getScreenName()

    override fun registerRoutes(
        navGraphBuilder: NavGraphBuilder,
        navController: NavHostController,
        loadingViewModel: LoadingViewModel,
        localImageLoaderValue: ProvidedValue<*>,
        getCurrentUser: () -> UserInstance?,
        getHomeRoute: () -> String,
        onNavigateToForgotPassword: () -> Unit,
        onNavigateToSignIn: () -> Unit
    ) {
        var localSecret = ""
        var backupCode = ""
        var isEnable2FAFlow = false

        navGraphBuilder.composable(
            route = Privacy.getScreenName(),
            enterTransition = DefaultNavAnimations.enter,
            popEnterTransition = DefaultNavAnimations.popEnter,
            exitTransition = DefaultNavAnimations.exit,
            popExitTransition = DefaultNavAnimations.popExit
        ) {
            val user = getCurrentUser()
            val loginHistoryViewModel: LoginHistoryViewModel = koinViewModel()
            if (user != null) {
                Privacy.PrivacyScreen(
                    currentUser = user,
                    onAcknowledgePrivacyRead = {
                        loginHistoryViewModel.acknowledgePrivacyRead(user)
                    },
                    onClickBack = { navController.popBackStack() }
                )
            } else {
                showToast("Cannot get your information now. Please try again!")
            }
        }

        navGraphBuilder.composable(
            route = SecuritySettings.getScreenName(),
            enterTransition = DefaultNavAnimations.enter,
            popEnterTransition = DefaultNavAnimations.popEnter,
            exitTransition = DefaultNavAnimations.exit,
            popExitTransition = DefaultNavAnimations.popExit
        ) {
            val user = getCurrentUser()
            if (user != null) {
                SecuritySettings.SecuritySettingsScreen(
                    currentUser = user,
                    paddingValues = androidx.compose.foundation.layout.PaddingValues(),
                    onNavigateBack = { navController.popBackStack() },
                    onChangePassword = { navController.navigate(ChangePassword.getScreenName()) },
                    onNavigateTo2FAScreen = { navController.navigate(TwoFA.getScreenName()) },
                    onLoginActivity = { navController.navigate(LoginHistory.getScreenName()) }
                )
            } else {
                showToast("Cannot get your information now. Please try again!")
            }
        }

        navGraphBuilder.composable(
            route = ChangePassword.getScreenName(),
            enterTransition = DefaultNavAnimations.enter,
            popEnterTransition = DefaultNavAnimations.popEnter,
            exitTransition = DefaultNavAnimations.exit,
            popExitTransition = DefaultNavAnimations.popExit
        ) {
            ChangePassword.ChangePasswordScreen(
                paddingValues = androidx.compose.foundation.layout.PaddingValues(),
                currentUser = getCurrentUser() ?: UserInstance(),
                loadingViewModel = loadingViewModel,
                onNavigateToForgotPasswordScreen = onNavigateToForgotPassword,
                onNavigateToSignInScreen = onNavigateToSignIn,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        navGraphBuilder.composable(
            route = TwoFA.getScreenName(),
            enterTransition = DefaultNavAnimations.enter,
            popEnterTransition = DefaultNavAnimations.popEnter,
            exitTransition = DefaultNavAnimations.exit,
            popExitTransition = DefaultNavAnimations.popExit
        ) {
            TwoFA.TwoFAScreen(
                paddingValues = androidx.compose.foundation.layout.PaddingValues(),
                currentUser = getCurrentUser() ?: UserInstance(),
                onContinue = { secret ->
                    localSecret = secret
                    navController.navigate(VerifyOTP.getScreenName())
                },
                onNavigateBack = {
                    localSecret = ""
                    navController.popBackStack()
                }
            )
        }

        navGraphBuilder.composable(
            route = VerifyOTP.getScreenName(),
            enterTransition = DefaultNavAnimations.enter,
            popEnterTransition = DefaultNavAnimations.popEnter,
            exitTransition = DefaultNavAnimations.exit,
            popExitTransition = DefaultNavAnimations.popExit
        ) {
            VerifyOTP.VerifyOTPScreen(
                paddingValues = androidx.compose.foundation.layout.PaddingValues(),
                localImageLoaderValue = localImageLoaderValue,
                currentUser = getCurrentUser() ?: UserInstance(),
                secretKey = localSecret,
                loadingViewModel = loadingViewModel,
                onNavigateToVerifyOTPSuccessScreen = { code ->
                    backupCode = code
                    isEnable2FAFlow = true
                    navController.navigate(TwoFactorEnabled.getScreenName())
                },
                onNavigateToHomeScreen = {
                    navController.navigate(getHomeRoute()) {
                        popUpTo(0) { inclusive = true }
                        launchSingleTop = true
                    }
                },
                onNavigateToBackupCodeScreen = {
                    navController.navigate(BackUpCode.getScreenName())
                },
                onNavigateBack = { navController.popBackStack() },
                onNavigateToSignInScreen = onNavigateToSignIn
            )
        }

        navGraphBuilder.composable(
            route = TwoFactorEnabled.getScreenName(),
            enterTransition = DefaultNavAnimations.enter,
            popEnterTransition = DefaultNavAnimations.popEnter,
            exitTransition = DefaultNavAnimations.exit,
            popExitTransition = DefaultNavAnimations.popExit
        ) {
            TwoFactorEnabled.TwoFactorEnabledScreen(
                paddingValues = androidx.compose.foundation.layout.PaddingValues(),
                backupCode = backupCode,
                isEnable2FAFlow = isEnable2FAFlow,
                onReturnClick = {
                    if (isEnable2FAFlow) {
                        navController.popBackStack(SecuritySettings.getScreenName(), false)
                    } else {
                        navController.navigate(getHomeRoute()) {
                            popUpTo(0) { inclusive = true }
                            launchSingleTop = true
                        }
                    }
                }
            )
        }

        navGraphBuilder.composable(
            route = BackUpCode.getScreenName(),
            enterTransition = DefaultNavAnimations.enter,
            popEnterTransition = DefaultNavAnimations.popEnter,
            exitTransition = DefaultNavAnimations.exit,
            popExitTransition = DefaultNavAnimations.popExit
        ) {
            BackUpCode.BackupCodeScreen(
                paddingValues = androidx.compose.foundation.layout.PaddingValues(),
                loadingViewModel = loadingViewModel,
                onNavigateBack = { navController.popBackStack() },
                onNavigateToVerifyBackupCodeSuccessScreen = { newBackupCode ->
                    backupCode = newBackupCode
                    isEnable2FAFlow = false
                    navController.navigate(TwoFactorEnabled.getScreenName())
                }
            )
        }

        navGraphBuilder.composable(
            route = LoginHistory.getScreenName(),
            enterTransition = DefaultNavAnimations.enter,
            popEnterTransition = DefaultNavAnimations.popEnter,
            exitTransition = DefaultNavAnimations.exit,
            popExitTransition = DefaultNavAnimations.popExit
        ) {
            LoginHistory.LoginHistoryScreen(
                currentUser = getCurrentUser() ?: UserInstance(),
                modifier = androidx.compose.ui.Modifier,
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}

