package com.minhtu.firesocialmedia

import com.minhtu.firesocialmedia.IosScreen.CommentScreen
import com.minhtu.firesocialmedia.IosScreen.ForgotPasswordScreen
import com.minhtu.firesocialmedia.IosScreen.FriendScreen
import com.minhtu.firesocialmedia.IosScreen.HomeScreen
import com.minhtu.firesocialmedia.IosScreen.InformationScreen
import com.minhtu.firesocialmedia.IosScreen.LoadingScreen
import com.minhtu.firesocialmedia.IosScreen.NotificationScreen
import com.minhtu.firesocialmedia.IosScreen.PostInformationScreen
import com.minhtu.firesocialmedia.IosScreen.SearchScreen
import com.minhtu.firesocialmedia.IosScreen.SettingsScreen
import com.minhtu.firesocialmedia.IosScreen.ShowImageScreen
import com.minhtu.firesocialmedia.IosScreen.SignInScreen
import com.minhtu.firesocialmedia.IosScreen.SignUpScreen
import com.minhtu.firesocialmedia.IosScreen.UploadNewsScreen
import com.minhtu.firesocialmedia.IosScreen.UserInformationScreen

sealed class IosScreen {
    object SignInScreen : IosScreen()
    object SignUpScreen : IosScreen()
    object HomeScreen : IosScreen()
    object UploadNewsScreen : IosScreen()
    object CommentScreen : IosScreen()
    object FriendScreen : IosScreen()
    object NotificationScreen : IosScreen()
    object SettingsScreen : IosScreen()
    object PostInformationScreen : IosScreen()
    object SearchScreen : IosScreen()
    object ShowImageScreen : IosScreen()
    object UserInformationScreen : IosScreen()
    object ForgotPasswordScreen : IosScreen()
    object InformationScreen : IosScreen()
    object LoadingScreen : IosScreen()

    override fun toString(): String = this::class.simpleName ?: super.toString()
}

fun String.toIosScreen(): IosScreen = when (this) {
    "SignInScreen" -> SignInScreen
    "SignUpScreen" -> SignUpScreen
    "HomeScreen" -> HomeScreen
    "UploadNewsScreen" -> UploadNewsScreen
    "CommentScreen" -> CommentScreen
    "FriendScreen" -> FriendScreen
    "NotificationScreen" -> NotificationScreen
    "SettingsScreen" -> SettingsScreen
    "PostInformationScreen" -> PostInformationScreen
    "SearchScreen" -> SearchScreen
    "ShowImageScreen" -> ShowImageScreen
    "UserInformationScreen" -> UserInformationScreen
    "ForgotPasswordScreen" -> ForgotPasswordScreen
    "InformationScreen" -> InformationScreen
    "LoadingScreen" -> LoadingScreen
    else -> SignInScreen // fallback/default
}