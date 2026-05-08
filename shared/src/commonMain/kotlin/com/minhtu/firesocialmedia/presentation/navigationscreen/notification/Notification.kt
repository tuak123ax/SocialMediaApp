package com.minhtu.firesocialmedia.presentation.navigationscreen.notification

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.IosShare
import androidx.compose.material.icons.filled.ModeComment
import androidx.compose.material.icons.filled.PersonAddAlt1
import androidx.compose.material.icons.filled.PostAdd
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.ProvidedValue
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.minhtu.firesocialmedia.constants.TestTag
import com.minhtu.firesocialmedia.domain.entity.news.NewsInstance
import com.minhtu.firesocialmedia.domain.entity.notification.NotificationInstance
import com.minhtu.firesocialmedia.domain.entity.notification.NotificationType
import com.minhtu.firesocialmedia.domain.entity.user.UserInstance
import com.minhtu.firesocialmedia.platform.CrossPlatformIcon
import com.minhtu.firesocialmedia.platform.showToast
import com.minhtu.firesocialmedia.platform.toHex
import com.minhtu.firesocialmedia.presentation.home.HomeViewModel
import com.minhtu.firesocialmedia.presentation.loading.Loading
import com.minhtu.firesocialmedia.presentation.loading.LoadingViewModel
import com.minhtu.firesocialmedia.presentation.search.SearchViewModel
import com.minhtu.firesocialmedia.storage.toStorageUrl
import com.minhtu.sharedmodule.ui.theme.adminCardColor
import com.minhtu.sharedmodule.ui.theme.blurLikeColor
import com.seiko.imageloader.ui.AutoSizeImage
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

class Notification {
    companion object{
        @Composable
        fun NotificationScreen(modifier: Modifier,
                               paddingValues: PaddingValues,
                               localImageLoaderValue : ProvidedValue<*>,
                               searchViewModel: SearchViewModel,
                               homeViewModel: HomeViewModel,
                               notificationViewModel : NotificationViewModel,
                               loadingViewModel: LoadingViewModel,
                               onNavigateToPostInformation: (new : NewsInstance) -> Unit,
                               onNavigateToUserInformation: (user : UserInstance?) -> Unit,
                               onNavigateToGroupDetails : (groupId : String) -> Unit){
            val isLoading by loadingViewModel.isLoading.collectAsState()
            val getNeededUsersStatus by notificationViewModel.getNeededUsersStatus.collectAsState()
            val getAllNotificationsStatus = homeViewModel.getAllNotificationsOfCurrentUser.value
            var showDropDownMenu by remember { mutableStateOf(false) }
            LaunchedEffect(Unit) {
                // Only show loading if notifications haven't been loaded yet
                if (!getAllNotificationsStatus) {
                    loadingViewModel.showLoading()
                }
                //Get more users to show notification information
                notificationViewModel.checkUsersInCacheAndGetMore(
                    homeViewModel.loadedUsersCache,
                    homeViewModel.listNotificationOfCurrentUser)
            }
            
            // Hide loading when both notifications are loaded and users are ready
            LaunchedEffect(getAllNotificationsStatus, getNeededUsersStatus) {
                if (getAllNotificationsStatus && getNeededUsersStatus) {
                    loadingViewModel.hideLoading()
                }
            }

            val deleteAllNotificationsStatus by notificationViewModel.deleteAllNotificationsStatus.collectAsState()
            LaunchedEffect(deleteAllNotificationsStatus) {
                if(deleteAllNotificationsStatus != null) {
                    if(deleteAllNotificationsStatus!!.success) {
                        showToast("Delete all notifications successfully!")
                        //Clear current notification data
                        homeViewModel.listNotificationOfCurrentUser.clear()
                    } else {
                        showToast(deleteAllNotificationsStatus!!.message)
                    }
                    notificationViewModel.resetDeleteAllNotificationsStatus()
                }
            }
            Column(
                verticalArrangement = Arrangement.Top,
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = modifier.padding(paddingValues)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.background)
                        .padding(10.dp)
                ) {
                    val iconSize = 35.dp
                    val sideSlotWidth = iconSize + 16.dp
                    // LEFT EMPTY SLOT
                    Box(
                        modifier = Modifier.width(sideSlotWidth)
                    )
                    // CENTER TITLE
                    Box(
                        modifier = Modifier.weight(1f),
                        contentAlignment = Alignment.Center
                    ) {

                        Text(
                            text = "Notifications",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center,
                            maxLines = 1
                        )
                    }
                    // RIGHT ICON SLOT
                    Box(
                        modifier = Modifier.width(sideSlotWidth),
                        contentAlignment = Alignment.CenterEnd
                    ) {
                        CrossPlatformIcon(
                            icon = "more_horiz",
                            backgroundColor = MaterialTheme.colorScheme.surface.toHex(),
                            contentDescription = "More Options",
                            tint = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier
                                .size(iconSize)
                                .clip(CircleShape)
                                .testTag(TestTag.TAG_BUTTON_MOREOPTIONS)
                                .semantics {
                                    contentDescription =
                                        TestTag.TAG_BUTTON_MOREOPTIONS
                                }
                                .clickable {
                                    showDropDownMenu = true
                                }
                                .padding(4.dp)
                        )

                        DropdownMenuForNotification(
                            showDropDownMenu,
                            onDismissRequest = {
                                showDropDownMenu = false
                            },
                            onDeleteAll = {
                                if (homeViewModel.currentUser != null) {
                                    notificationViewModel.deleteAllNotifications(
                                        homeViewModel.currentUser!!
                                    )
                                }
                            }
                        )
                    }
                }

                HorizontalDivider(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 10.dp),
                    thickness = 1.dp,
                    color = MaterialTheme.colorScheme.outlineVariant
                )
                //Sort notification list by timeSend
                val notificationList = remember(homeViewModel.listNotificationOfCurrentUser) {
                    homeViewModel.listNotificationOfCurrentUser.sortedByDescending { it.timeSend }
                }
                LazyColumn(
                    modifier = Modifier
                        .testTag(TestTag.TAG_NOTIFICATION_LIST)
                        .semantics {
                            contentDescription = TestTag.TAG_NOTIFICATION_LIST
                        }
                ) {
                    if(getNeededUsersStatus && homeViewModel.listNotificationOfCurrentUser.isNotEmpty()) {
                        items(notificationList, key = { it.id }) { notification ->
                            //State to track visibility of a notification
                            var visible by remember { mutableStateOf(true) }
                            //State to track to delay before delete data from db
                            var pendingDelete by remember { mutableStateOf(false) }
                            if (pendingDelete) {
                                // wait for animation before removing
                                LaunchedEffect(Unit) {
                                    delay(200)
                                    homeViewModel.removeNotificationInList(notification)
                                    homeViewModel.deleteNotification(notification)
                                }
                            }

                            val user = notificationViewModel.findLoadedUserInSet(notification.sender)

                            if(user != null) {
                                AnimatedVisibility(
                                    visible = visible,
                                    exit = slideOutHorizontally(
                                        targetOffsetX = { fullWidth -> fullWidth },
                                        animationSpec = tween(durationMillis = 200)
                                    )
                                ) {
                                    NotificationHasSwipeToDelete(
                                        notification,
                                        user,
                                        localImageLoaderValue,
                                        homeViewModel,
                                        notificationViewModel,
                                        onDelete = {
                                            visible = false
                                            pendingDelete = true
                                        },
                                        onNavigateToPostInformation,
                                        onNavigateToUserInformation,
                                        onNavigateToGroupDetails
                                    )
                                }
                            }
                        }
                    } else if (getNeededUsersStatus && homeViewModel.listNotificationOfCurrentUser.isEmpty()) {
                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(32.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "No notifications yet",
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
                if (isLoading) {
                    Loading.LoadingScreen()
                }
            }
        }

        @Composable
        fun NotificationHasSwipeToDelete(notification: NotificationInstance,
                                         user: UserInstance,
                                         localImageLoaderValue : ProvidedValue<*>,
                                         homeViewModel: HomeViewModel,
                                         notificationViewModel: NotificationViewModel,
                                         onDelete: () -> Unit,
                                         onNavigateToPostInformation: (new : NewsInstance) -> Unit,
                                         onNavigateToUserInformation: (user : UserInstance?) -> Unit,
                                         onNavigateToGroupDetails : (groupId : String) -> Unit) {
            val swipeDistancePx = with(LocalDensity.current) { 70.dp.toPx() }
            var offsetX by remember { mutableFloatStateOf(0f) }
            val animatedOffsetX by animateFloatAsState(targetValue = offsetX)
            val swipeThreshold = -swipeDistancePx / 2
            val currentUser = homeViewModel.currentUser
            val coroutineScope = rememberCoroutineScope()
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(IntrinsicSize.Min)
                    .testTag(TestTag.TAG_NOTIFICATION)
                    .semantics {
                        contentDescription = TestTag.TAG_NOTIFICATION
                    }
            ) {
                //Row contains delete button
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(if(notification.beRead) MaterialTheme.colorScheme.surface else adminCardColor)
                        .testTag(TestTag.TAG_BUTTON_DELETE)
                        .semantics {
                            contentDescription = TestTag.TAG_BUTTON_DELETE
                        },
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .padding(end = 16.dp)
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.error)
                            .clickable { onDelete() },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Delete",
                            tint = MaterialTheme.colorScheme.onError,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }

                // Foreground content (slideable)
                Box(
                    modifier = Modifier
                        .offset { IntOffset(animatedOffsetX.roundToInt(), 0) }
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.surface)
                        .pointerInput(notification.id) {
                            detectHorizontalDragGestures(
                                onHorizontalDrag = { _, dragAmount ->
                                    val newOffset =
                                        (offsetX + dragAmount).coerceIn(-swipeDistancePx, 0f)
                                    offsetX = newOffset
                                },
                                onDragEnd = {
                                    offsetX = if (offsetX < swipeThreshold) -swipeDistancePx else 0f
                                }
                            )
                        }
                        .clickable {
                            if (notification.relatedInfo.isNotEmpty()) {
                                if (notification.type == NotificationType.LIKE ||
                                    notification.type == NotificationType.COMMENT ||
                                    notification.type == NotificationType.UPLOAD_NEW||
                                    notification.type == NotificationType.SHARE_NEW
                                ) {
                                    notificationViewModel.onNotificationClick(
                                        notification,
                                        homeViewModel.listNews,
                                        onNavigateToPostInformation = { relatedNew ->
                                            onNavigateToPostInformation(relatedNew)
                                        },
                                        onError = {
                                            showToast("Cannot find the related post!")
                                        })
                                } else {
                                    if (notification.type == NotificationType.ADD_FRIEND) {
                                        onNavigateToUserInformation(
                                            user
                                        )
                                    } else {
                                        if(notification.type == NotificationType.INVITE_TO_GROUP) {
                                            onNavigateToGroupDetails(
                                                notification.relatedInfo
                                            )
                                        }
                                    }
                                }
                            } else {
                                showToast("This notification is from old version, cannot navigate to other screen!")
                            }
                            //Mark as read
                            if(currentUser != null) {
                                coroutineScope.launch {
                                    val updatedNotification = notification.copy(beRead = true)
                                    notificationViewModel.updateIsReadStatusOfNotification(
                                        updatedNotification,
                                        currentUser
                                    )
                                    val updatedNotifications = homeViewModel.listNotificationOfCurrentUser.map {
                                        if (it.id == updatedNotification.id) updatedNotification else it
                                    }

                                    homeViewModel.listNotificationOfCurrentUser.clear()
                                    homeViewModel.listNotificationOfCurrentUser.addAll(updatedNotifications)
                                }
                            }
                        }
                ) {
                    NotificationRow(notification, user, localImageLoaderValue)
                }
            }

        }
        @Composable
        fun NotificationRow(notification: NotificationInstance,
                            user: UserInstance,
                            localImageLoaderValue : ProvidedValue<*>) {
            Row(
                horizontalArrangement = Arrangement.Start,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        color = if(notification.beRead) MaterialTheme.colorScheme.surface else adminCardColor
                    )
            ) {
                if(!notification.beRead) {
                    VerticalDivider(
                        modifier = Modifier,
                        thickness = 3.dp,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                CompositionLocalProvider(
                    localImageLoaderValue
                ) {
                    AutoSizeImage(
                        notification.avatar.toStorageUrl(),
                        contentDescription = "Avatar",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .size(60.dp)
                            .padding(horizontal = 10.dp, vertical = 8.dp)
                            .clip(CircleShape)
                            .graphicsLayer {
                                if(notification.beRead) alpha = 0.75f
                            }
                    )
                }

                Spacer(modifier = Modifier.width(10.dp))

                Column(
                    modifier = Modifier
                        .weight(1f) // Take up remaining space
                        .padding(end = 8.dp)
                ) {
                    Text(
                        text = user.name,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        style = MaterialTheme.typography.bodyLarge,
                        color = if(notification.beRead) blurLikeColor.copy(alpha = 0.75f) else MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = when (notification.type) {
                            NotificationType.LIKE -> "liked your post!"
                            NotificationType.COMMENT -> "commented in your post!"
                            NotificationType.ADD_FRIEND -> "sent you a friend request!"
                            NotificationType.UPLOAD_NEW -> "uploaded a new post!"
                            NotificationType.SHARE_NEW -> "shared a post!"
                            NotificationType.INVITE_TO_GROUP -> "invited you to a group!"
                            else -> {
                                "Unknown notification type!"
                            }
                        },
                        color = if(notification.beRead) blurLikeColor.copy(alpha = 0.75f) else MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }

                ShowBasedOnNotificationType(
                    notification.type,
                    Modifier.graphicsLayer {
                        if(notification.beRead) alpha = 0.5f
                    })
            }

        }

        @Composable
        private fun ShowBasedOnNotificationType(type: NotificationType,
                                                modifier: Modifier = Modifier) {
            when(type) {
                NotificationType.LIKE -> {
                    IconButton(onClick = { /* Handle click */ }) {
                        Icon(
                            imageVector = Icons.Filled.Favorite,
                            contentDescription = "Like",
                            tint = MaterialTheme.colorScheme.primary, // Pink/Like
                            modifier = modifier
                        )
                    }
                }
                NotificationType.COMMENT -> {
                    IconButton(onClick = { /* Handle click */ }) {
                        Icon(
                            imageVector = Icons.Filled.ModeComment,
                            contentDescription = "Comment",
                            tint = MaterialTheme.colorScheme.onSurface,
                            modifier = modifier
                        )
                    }
                }
                NotificationType.ADD_FRIEND -> {
                    IconButton(onClick = { /* Handle click */ }) {
                        Icon(
                            imageVector = Icons.Filled.PersonAddAlt1,
                            contentDescription = "Add friend",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = modifier
                        )
                    }
                }

                NotificationType.UPLOAD_NEW -> {
                    IconButton(onClick = { /* Handle click */ }) {
                        Icon(
                            imageVector = Icons.Filled.PostAdd,
                            contentDescription = "Upload new",
                            tint = MaterialTheme.colorScheme.tertiary,
                            modifier = modifier
                        )
                    }
                }

                NotificationType.SHARE_NEW -> {
                    IconButton(onClick = { /* Handle click */ }) {
                        Icon(
                            imageVector = Icons.Filled.IosShare,
                            contentDescription = "Shared Post",
                            tint = MaterialTheme.colorScheme.tertiary,
                            modifier = modifier
                        )
                    }
                }
                NotificationType.INVITE_TO_GROUP -> {
                    IconButton(onClick = { /* Handle click */ }) {
                        Icon(
                            imageVector = Icons.Default.Group,
                            contentDescription = "Invite to group",
                            tint = MaterialTheme.colorScheme.secondary,
                            modifier = modifier
                        )
                    }
                }
                else -> {
                    IconButton(onClick = { /* Handle click */ }) {
                        Icon(
                            imageVector = Icons.Default.Error,
                            contentDescription = "Error",
                            tint = MaterialTheme.colorScheme.error,
                            modifier = modifier
                        )
                    }
                }

            }
        }

        @Composable
        fun DropdownMenuForNotification(expanded : Boolean,
                                        onDismissRequest: () -> Unit,
                                        onDeleteAll: () -> Unit) {
            DropdownMenu(
                expanded = expanded,
                onDismissRequest = onDismissRequest
            ) {
                DropdownMenuItem(
                    text = { Text("Delete All") },
                    onClick = {
                        onDeleteAll()
                        onDismissRequest()
                    }
                )
            }
        }

        fun getScreenName() : String {
            return "NotificationScreen"
        }
    }
}