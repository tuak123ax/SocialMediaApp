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
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.ModeComment
import androidx.compose.material.icons.filled.PersonAddAlt1
import androidx.compose.material.icons.filled.PostAdd
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.ProvidedValue
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
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
import androidx.compose.ui.unit.sp
import com.minhtu.firesocialmedia.constants.TestTag
import com.minhtu.firesocialmedia.domain.entity.news.NewsInstance
import com.minhtu.firesocialmedia.domain.entity.notification.NotificationInstance
import com.minhtu.firesocialmedia.domain.entity.notification.NotificationType
import com.minhtu.firesocialmedia.domain.entity.user.UserInstance
import com.minhtu.firesocialmedia.platform.generateImageLoader
import com.minhtu.firesocialmedia.platform.logMessage
import com.minhtu.firesocialmedia.platform.showToast
import com.minhtu.firesocialmedia.presentation.home.HomeViewModel
import com.minhtu.firesocialmedia.presentation.loading.Loading
import com.minhtu.firesocialmedia.presentation.loading.LoadingViewModel
import com.minhtu.firesocialmedia.presentation.search.SearchViewModel
import com.seiko.imageloader.LocalImageLoader
import com.seiko.imageloader.ui.AutoSizeImage
import kotlinx.coroutines.delay
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
                               onNavigateToUserInformation: (user : UserInstance?) -> Unit){
            val isLoading by loadingViewModel.isLoading.collectAsState()
            val getNeededUsersStatus by notificationViewModel.getNeededUsersStatus.collectAsState()
            val getAllNotificationsStatus = homeViewModel.getAllNotificationsOfCurrentUser.value
            
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
            Column(
                verticalArrangement = Arrangement.Top,
                horizontalAlignment = Alignment.Companion.CenterHorizontally,
                modifier = modifier.padding(paddingValues)
            ) {
                Text(
                    text = "Notifications",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Companion.Bold,
                    textAlign = TextAlign.Companion.Center,
                    modifier = Modifier.Companion.fillMaxWidth().padding(vertical = 20.dp)
                )
                //Sort notification list by timeSend
                val notificationList = remember(homeViewModel.listNotificationOfCurrentUser) {
                    homeViewModel.listNotificationOfCurrentUser.sortedByDescending { it.timeSend }
                }
                LazyColumn(
                    modifier = Modifier.Companion
                        .testTag(TestTag.Companion.TAG_NOTIFICATION_LIST)
                        .semantics {
                            contentDescription = TestTag.Companion.TAG_NOTIFICATION_LIST
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
                                        onNavigateToUserInformation
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
                    Loading.Companion.LoadingScreen()
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
                                         onNavigateToUserInformation: (user : UserInstance?) -> Unit) {
            val swipeDistancePx = with(LocalDensity.current) { 70.dp.toPx() }
            var offsetX by remember { mutableFloatStateOf(0f) }
            val animatedOffsetX by animateFloatAsState(targetValue = offsetX)
            val swipeThreshold = -swipeDistancePx / 2

            Box(
                modifier = Modifier.Companion
                    .fillMaxWidth()
                    .height(IntrinsicSize.Min)
                    .testTag(TestTag.Companion.TAG_NOTIFICATION)
                    .semantics {
                        contentDescription = TestTag.Companion.TAG_NOTIFICATION
                    }
            ) {
                //Row contains delete button
                Row(
                    modifier = Modifier.Companion
                        .fillMaxSize()
                        .background(Color.Companion.White)
                        .testTag(TestTag.Companion.TAG_BUTTON_DELETE)
                        .semantics {
                            contentDescription = TestTag.Companion.TAG_BUTTON_DELETE
                        },
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.Companion.CenterVertically
                ) {
                    Box(
                        modifier = Modifier.Companion
                            .padding(end = 16.dp)
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(Color.Companion.Red)
                            .clickable { onDelete() },
                        contentAlignment = Alignment.Companion.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Delete",
                            tint = Color.Companion.White,
                            modifier = Modifier.Companion.size(24.dp)
                        )
                    }
                }

                // Foreground content (slidable)
                Box(
                    modifier = Modifier.Companion
                        .offset { IntOffset(animatedOffsetX.roundToInt(), 0) }
                        .fillMaxWidth()
                        .background(Color.Companion.White)
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
                                    notification.type == NotificationType.UPLOAD_NEW
                                ) {
                                    logMessage(
                                        "onNavigateToPostInformation",
                                        { notification.relatedInfo }
                                    )
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
                                    }
                                }
                            } else {
                                showToast("This notification is from old version, cannot navigate to other screen!")
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
                verticalAlignment = Alignment.Companion.CenterVertically,
                modifier = Modifier.Companion
                    .fillMaxWidth()
                    .padding(horizontal = 10.dp, vertical = 8.dp)
            ) {
                CompositionLocalProvider(
                    localImageLoaderValue
                ) {
                    AutoSizeImage(
                        notification.avatar,
                        contentDescription = "Avatar",
                        contentScale = ContentScale.Companion.Crop,
                        modifier = Modifier.Companion
                            .size(60.dp)
                            .clip(CircleShape)
                    )
                }

                Spacer(modifier = Modifier.Companion.width(10.dp))

                Column(
                    modifier = Modifier.Companion
                        .weight(1f) // Take up remaining space
                        .padding(end = 8.dp)
                ) {
                    Text(
                        text = user.name,
                        color = Color.Companion.Black,
                        maxLines = 1,
                        overflow = TextOverflow.Companion.Ellipsis,
                        style = MaterialTheme.typography.bodyLarge
                    )
                    Text(
                        text = when (notification.type) {
                            NotificationType.NONE -> ""
                            NotificationType.LIKE -> "liked your post!"
                            NotificationType.COMMENT -> "commented in your post!"
                            NotificationType.ADD_FRIEND -> "sent you a friend request!"
                            NotificationType.UPLOAD_NEW -> "uploaded a new post!"
                        },
                        color = Color.Companion.Gray,
                        maxLines = 1,
                        overflow = TextOverflow.Companion.Ellipsis,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }

                ShowBasedOnNotificationType(notification.type)
            }

        }

        @Composable
        private fun ShowBasedOnNotificationType(type: NotificationType) {
            when(type) {
                NotificationType.NONE -> {

                }
                NotificationType.LIKE -> {
                    IconButton(onClick = { /* Handle click */ }) {
                        Icon(
                            imageVector = Icons.Filled.Favorite,
                            contentDescription = "Like",
                            tint = Color(0xFFFF4081) // Pink
                        )
                    }
                }
                NotificationType.COMMENT -> {
                    IconButton(onClick = { /* Handle click */ }) {
                        Icon(
                            imageVector = Icons.Filled.ModeComment,
                            contentDescription = "Comment",
                            tint = Color.Companion.Black
                        )
                    }
                }
                NotificationType.ADD_FRIEND -> {
                    IconButton(onClick = { /* Handle click */ }) {
                        Icon(
                            imageVector = Icons.Filled.PersonAddAlt1,
                            contentDescription = "Add friend",
                            tint = Color.Companion.Blue
                        )
                    }
                }

                NotificationType.UPLOAD_NEW -> {
                    IconButton(onClick = { /* Handle click */ }) {
                        Icon(
                            imageVector = Icons.Filled.PostAdd,
                            contentDescription = "Upload new",
                            tint = Color.Companion.Green
                        )
                    }
                }
            }
        }

        fun getScreenName() : String {
            return "NotificationScreen"
        }
    }
}