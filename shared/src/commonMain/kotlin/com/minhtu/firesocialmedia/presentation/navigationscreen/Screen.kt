package com.minhtu.firesocialmedia.presentation.navigationscreen

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.ui.graphics.vector.ImageVector

sealed class Screen(val title : String, val route : String,val icon : ImageVector)
{
    data object Home : Screen("Home", com.minhtu.firesocialmedia.presentation.home.Home.getScreenName(), Icons.Default.Home)
    data object Friend : Screen("Friend", com.minhtu.firesocialmedia.presentation.navigationscreen.friend.Friend.getScreenName(), Icons.Default.Person)
    data object Notification : Screen("Notification", com.minhtu.firesocialmedia.presentation.navigationscreen.notification.Notification.getScreenName(), Icons.Default.Notifications)
    data object Settings : Screen("Settings", com.minhtu.firesocialmedia.presentation.navigationscreen.setting.Settings.getScreenName(), Icons.Default.Settings)
}