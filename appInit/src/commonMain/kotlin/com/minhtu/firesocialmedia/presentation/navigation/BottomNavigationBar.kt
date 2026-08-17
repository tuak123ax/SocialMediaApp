package com.minhtu.firesocialmedia.presentation.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsBottomHeight
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.minhtu.firesocialmedia.presentation.navigationscreen.Screen

private const val TAG_NOTIFICATION_BOTTOM = "TAG_NOTIFICATION_BOTTOM"
private const val TAG_HOME_BOTTOM = "TAG_HOME_BOTTOM"
private const val TAG_FRIEND_BOTTOM = "TAG_FRIEND_BOTTOM"
private const val TAG_SETTING_BOTTOM = "TAG_SETTING_BOTTOM"
private const val TAG_BOTTOM_ACTION_BUTTON = "TAG_BOTTOM_ACTION_BUTTON"

@Composable
fun BottomNavigationBar(
    currentRoute: String?,
    onNavigate: (String) -> Unit,
    unreadNotificationCount: Int,
    onNavigateToUploadNews: () -> Unit,
    modifier: Modifier
) {
    val items = listOf(
        Screen.Home,
        Screen.Friend,
        Screen.Notification,
        Screen.Settings
    )
    Column(modifier = modifier) {
        Box(modifier = Modifier.fillMaxWidth()) {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface,
                windowInsets = WindowInsets(0),
                modifier = Modifier.height(60.dp)
            ) {
                val currentRoute = currentRoute
                items.forEach { screen ->
                    val notificationCount = unreadNotificationCount

                    val showBadge = screen.route == Screen.Notification.route && notificationCount > 0
                    val testTag = when(screen.route) {
                        Screen.Notification.route -> TAG_NOTIFICATION_BOTTOM
                        Screen.Home.route -> TAG_HOME_BOTTOM
                        Screen.Friend.route -> TAG_FRIEND_BOTTOM
                        Screen.Settings.route -> TAG_SETTING_BOTTOM
                        else -> ""
                    }
                    NavigationBarItem(
                        icon = {
                            if(showBadge) {
                                BadgedBox(
                                    badge = {
                                        Badge{
                                            Text(notificationCount.toString())
                                        }
                                    }
                                ) {
                                    Icon(screen.icon, contentDescription = screen.title) }
                                }
                            else {
                                Icon(screen.icon, contentDescription = screen.title) }
                            },
                        selected = currentRoute == screen.route,
                        onClick = { onNavigate(screen.route) },
                        modifier = Modifier
                            .testTag(testTag)
                            .semantics {
                                contentDescription = testTag
                            }
                    )
                }
            }

            //Floating action button
            FloatingActionButton(
                onClick = { onNavigateToUploadNews() },
                modifier = Modifier
                    .size(56.dp)
                    .align(Alignment.TopCenter)
                    .offset(y = (-28).dp)
                    .testTag(TAG_BOTTOM_ACTION_BUTTON)
                    .semantics{
                        contentDescription = TAG_BOTTOM_ACTION_BUTTON
                    },
                shape = CircleShape,
                containerColor = MaterialTheme.colorScheme.primary,
                elevation = FloatingActionButtonDefaults.elevation(
                    defaultElevation = 8.dp
                )
            ) {
                Icon(
                    Icons.Default.Add,
                    contentDescription = "Add",
                    tint = MaterialTheme.colorScheme.onPrimary
                )
            }
        }

        // Fill system navigation bar area with surface color so it doesn't show through
        Spacer(
            modifier = Modifier
                .fillMaxWidth()
                .windowInsetsBottomHeight(WindowInsets.navigationBars)
                .background(MaterialTheme.colorScheme.surface)
        )
    }
}
