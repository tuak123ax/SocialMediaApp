package com.minhtu.firesocialmedia.platform

import com.minhtu.firesocialmedia.platform.IosScreen.CommentScreen
import com.minhtu.firesocialmedia.platform.IosScreen.ForgotPasswordScreen
import com.minhtu.firesocialmedia.platform.IosScreen.FriendScreen
import com.minhtu.firesocialmedia.platform.IosScreen.HomeScreen
import com.minhtu.firesocialmedia.platform.IosScreen.InformationScreen
import com.minhtu.firesocialmedia.platform.IosScreen.LoadingScreen
import com.minhtu.firesocialmedia.platform.IosScreen.NotificationScreen
import com.minhtu.firesocialmedia.platform.IosScreen.PostInformationScreen
import com.minhtu.firesocialmedia.platform.IosScreen.SearchScreen
import com.minhtu.firesocialmedia.platform.IosScreen.SettingsScreen
import com.minhtu.firesocialmedia.platform.IosScreen.ShowImageScreen
import com.minhtu.firesocialmedia.platform.IosScreen.SignInScreen
import com.minhtu.firesocialmedia.platform.IosScreen.SignUpScreen
import com.minhtu.firesocialmedia.platform.IosScreen.UploadNewsScreen
import com.minhtu.firesocialmedia.platform.IosScreen.UserInformationScreen

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
    // Temporarily removed calling screens to get basic build working
    // object CallingScreen : IosScreen()
    // object VideoCallScreen : IosScreen()

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
    // Temporarily removed calling screens
    // "CallingScreen" -> CallingScreen
    // "VideoCallScreen" -> VideoCallScreen
    else -> SignInScreen // fallback/default
}