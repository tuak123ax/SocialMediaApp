package com.minhtu.firesocialmedia.home.navigationscreen.notification

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
import com.minhtu.firesocialmedia.PlatformContext
import com.minhtu.firesocialmedia.constants.TestTag
import com.minhtu.firesocialmedia.generateImageLoader
import com.minhtu.firesocialmedia.home.HomeViewModel
import com.minhtu.firesocialmedia.home.search.SearchViewModel
import com.minhtu.firesocialmedia.instance.NewsInstance
import com.minhtu.firesocialmedia.instance.NotificationInstance
import com.minhtu.firesocialmedia.instance.NotificationType
import com.minhtu.firesocialmedia.instance.UserInstance
import com.minhtu.firesocialmedia.logMessage
import com.minhtu.firesocialmedia.showToast
import com.minhtu.firesocialmedia.utils.Utils
import com.seiko.imageloader.LocalImageLoader
import com.seiko.imageloader.ui.AutoSizeImage
import kotlinx.coroutines.delay
import kotlin.math.roundToInt

class Notification {
    companion object{
        @Composable
        fun NotificationScreen(modifier: Modifier,
                               platform : PlatformContext,
                               paddingValues: PaddingValues,
                               searchViewModel: SearchViewModel,
                               homeViewModel: HomeViewModel,
                               onNavigateToPostInformation: (new : NewsInstance?) -> Unit,
                               onNavigateToUserInformation: (user : UserInstance?) -> Unit){
            Column(verticalArrangement = Arrangement.Top,horizontalAlignment = Alignment.CenterHorizontally, modifier = modifier.padding(paddingValues)) {
                Text(text = "Notifications",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth().padding(vertical = 20.dp)
                )
                //Sort notification list by timeSend
                val notificationList = remember(homeViewModel.listNotificationOfCurrentUser) {
                    homeViewModel.listNotificationOfCurrentUser.sortedByDescending { it.timeSend }
                }
                LazyColumn(modifier = Modifier
                    .testTag(TestTag.TAG_NOTIFICATION_LIST)
                    .semantics{
                        contentDescription = TestTag.TAG_NOTIFICATION_LIST
                    }) {
                    items(notificationList, key = {it.id}){notification ->
                        //State to track visibility of a notification
                        var visible by remember { mutableStateOf(true) }
                        //State to track to delay before delete data from db
                        var pendingDelete by remember { mutableStateOf(false) }
                        val user = Utils.findUserById(notification.sender, homeViewModel.listUsers)
                        if(user != null) {
                            if(pendingDelete) {
                                // wait for animation before removing
                                LaunchedEffect(Unit) {
                                    delay(200)
                                    homeViewModel.removeNotificationInList(notification)
                                    homeViewModel.deleteNotification(notification, platform)
                                }
                            }
                            AnimatedVisibility(
                                visible = visible,
                                exit = slideOutHorizontally(
                                    targetOffsetX = { fullWidth -> fullWidth },
                                    animationSpec = tween(durationMillis = 200)
                                )
                            ) {
                                NotificationHasSwipeToDelete(notification, user, homeViewModel,
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
                }
            }
        }

        @Composable
        fun NotificationHasSwipeToDelete(notification: NotificationInstance,
                                         user: UserInstance,
                                         homeViewModel: HomeViewModel,
                                         onDelete: () -> Unit,
                                         onNavigateToPostInformation: (new : NewsInstance?) -> Unit,
                                         onNavigateToUserInformation: (user : UserInstance?) -> Unit) {
            val swipeDistancePx = with(LocalDensity.current) { 70.dp.toPx() }
            var offsetX by remember { mutableFloatStateOf(0f) }
            val animatedOffsetX by animateFloatAsState(targetValue = offsetX)
            val swipeThreshold = -swipeDistancePx / 2

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(IntrinsicSize.Min)
                    .testTag(TestTag.TAG_NOTIFICATION)
                    .semantics{
                        contentDescription = TestTag.TAG_NOTIFICATION
                    }
            ) {
                //Row contains delete button
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.White)
                        .testTag(TestTag.TAG_BUTTON_DELETE)
                        .semantics{
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
                            .background(Color.Red)
                            .clickable { onDelete() },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Delete",
                            tint = Color.White,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }

                // Foreground content (slidable)
                Box(
                    modifier = Modifier
                        .offset { IntOffset(animatedOffsetX.roundToInt(), 0) }
                        .fillMaxWidth()
                        .background(Color.White)
                        .pointerInput(notification.id) {
                            detectHorizontalDragGestures(
                                onHorizontalDrag = { _, dragAmount ->
                                    val newOffset = (offsetX + dragAmount).coerceIn(-swipeDistancePx, 0f)
                                    offsetX = newOffset
                                },
                                onDragEnd = {
                                    offsetX = if (offsetX < swipeThreshold) -swipeDistancePx else 0f
                                }
                            )
                        }
                        .clickable {
                            if (notification.relatedInfo.isNotEmpty()) {
                                if(notification.type == NotificationType.LIKE ||
                                    notification.type == NotificationType.COMMENT ||
                                    notification.type == NotificationType.UPLOAD_NEW) {
                                    logMessage("onNavigateToPostInformation", notification.relatedInfo)
                                    val relatedNew = Utils.findNewById(notification.relatedInfo, homeViewModel.listNews)
                                    if(relatedNew != null) {
                                        onNavigateToPostInformation(relatedNew)
                                    } else {
                                        showToast("Cannot find the related post!")
                                    }
                                } else {
                                    if(notification.type == NotificationType.ADD_FRIEND) {
                                        onNavigateToUserInformation(Utils.findUserById(notification.relatedInfo, homeViewModel.listUsers))
                                    }
                                }
                            } else {
                                showToast("This notification is from old version, cannot navigate to other screen!")
                            }
                        }
                ) {
                    NotificationRow(notification, user)
                }
            }

        }
        @Composable
        fun NotificationRow(notification: NotificationInstance, user: UserInstance) {
            Row(
                horizontalArrangement = Arrangement.Start,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 10.dp, vertical = 8.dp)
            ) {
                CompositionLocalProvider(
                    LocalImageLoader provides remember { generateImageLoader() },
                ) {
                    AutoSizeImage(
                        notification.avatar,
                        contentDescription = "Avatar",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .size(60.dp)
                            .clip(CircleShape)
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
                        color = Color.Black,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
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
                        color = Color.Gray,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
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
                            tint = Color.Black
                        )
                    }
                }
                NotificationType.ADD_FRIEND -> {
                    IconButton(onClick = { /* Handle click */ }) {
                        Icon(
                            imageVector = Icons.Filled.PersonAddAlt1,
                            contentDescription = "Add friend",
                            tint = Color.Blue
                        )
                    }
                }

                NotificationType.UPLOAD_NEW -> {
                    IconButton(onClick = { /* Handle click */ }) {
                        Icon(
                            imageVector = Icons.Filled.PostAdd,
                            contentDescription = "Upload new",
                            tint = Color.Green
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