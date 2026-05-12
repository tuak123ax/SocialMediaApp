package com.minhtu.firesocialmedia.utils

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.windowInsetsBottomHeight
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.outlined.Block
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarDefaults
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.ProvidedValue
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.SoftwareKeyboardController
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.minhtu.firesocialmedia.constants.TestTag
import com.minhtu.firesocialmedia.domain.entity.home.deeplinks.ShareApp
import com.minhtu.firesocialmedia.domain.entity.news.NewsInstance
import com.minhtu.firesocialmedia.domain.entity.user.UserInstance
import com.minhtu.firesocialmedia.platform.CrossPlatformIcon
import com.minhtu.firesocialmedia.platform.VideoPlayer
import com.minhtu.firesocialmedia.platform.convertTimeToDateString
import com.minhtu.firesocialmedia.platform.getUriStringFromLocalPath
import com.minhtu.firesocialmedia.platform.launchShareAppWithDeepLink
import com.minhtu.firesocialmedia.platform.queryShareApps
import com.minhtu.firesocialmedia.platform.toHex
import com.minhtu.firesocialmedia.presentation.home.Home
import com.minhtu.firesocialmedia.presentation.home.HomeViewModel
import com.minhtu.firesocialmedia.presentation.navigationscreen.Screen
import com.minhtu.firesocialmedia.presentation.navigationscreen.friend.Friend
import com.minhtu.firesocialmedia.presentation.navigationscreen.friend.FriendViewModel
import com.minhtu.firesocialmedia.presentation.navigationscreen.notification.Notification
import com.minhtu.firesocialmedia.presentation.navigationscreen.setting.Settings
import com.minhtu.firesocialmedia.presentation.navigationscreen.setting.group.GroupDetails.Companion.DropdownMenuForMoreOptionsInGroup
import com.minhtu.firesocialmedia.presentation.search.SearchViewModel
import com.minhtu.firesocialmedia.storage.toStorageUrl
import com.seiko.imageloader.asImageBitmap
import com.seiko.imageloader.ui.AutoSizeImage
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

class UiUtils {
    companion object{
        @Composable
        fun NewsCard(
            news: NewsInstance,
            user : UserInstance,
            isLiked : Boolean,
            likeCountList : HashMap<String, Int>,
            commentCountList : HashMap<String, Int>,
            localImageLoaderValue : ProvidedValue<*>,
            likeCommentAndShareButtonEnable : Boolean,
            hasDropdownMenu : Boolean,
            onNavigateToShowImageScreen: (image: String) -> Unit,
            onNavigateToUserInformation: (user: UserInstance) -> Unit,
            homeViewModel: HomeViewModel,
            listState : LazyListState,
            onDelete: (action : String, new : NewsInstance) -> Unit,
            onNavigateToCreatePost : (updateNew : NewsInstance) -> Unit,
            showBottomSheet : (NewsInstance) -> Unit) {
            LaunchedEffect(Unit) {
                homeViewModel.updateLikeStatus()
            }
            Card(
                modifier = Modifier
                    .padding(start = 10.dp, end = 10.dp, top = 10.dp)
                    .fillMaxWidth()
                    .testTag(TestTag.TAG_POST_IN_COLUMN)
                    .semantics{
                        contentDescription = TestTag.TAG_POST_IN_COLUMN
                    },
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(1.dp)) {
                    Row(horizontalArrangement = Arrangement.Start,
                        modifier = Modifier.background(color = MaterialTheme.colorScheme.surface).padding(10.dp).fillMaxWidth()
                            .clickable {
                                onNavigateToUserInformation(user)
                            }){
                        CompositionLocalProvider(
                            localImageLoaderValue
                        ) {
                            val avatarUrl = news.avatar.toStorageUrl()
                            AutoSizeImage(
                                avatarUrl,
                                contentDescription = "Poster Avatar",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .testTag(TestTag.TAG_POSTER_AVATAR)
                                    .semantics{
                                        contentDescription = TestTag.TAG_POSTER_AVATAR
                                    }
                            )
                        }

                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = news.posterName,
                                color = MaterialTheme.colorScheme.onSurface,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 2.dp)
                            )
                            Text(
                                text = convertTimeToDateString(news.timePosted),
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(horizontal = 2.dp)
                            )
                        }
                        Spacer(modifier = Modifier.weight(1f))
                        //Box to add three dot icon and dropdownMenu when clicking the icon
                        if(hasDropdownMenu) {
                            Box{
                                var showMenu by remember { mutableStateOf(false) }
                                IconButton(onClick = {
                                    showMenu = true
                                }) {
                                    CrossPlatformIcon(
                                        icon = "more_horiz",
                                        backgroundColor = MaterialTheme.colorScheme.surface.toHex(),
                                        contentDescription = "More Options",
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier
                                            .testTag(TestTag.TAG_BUTTON_MOREOPTIONS)
                                            .semantics {
                                                contentDescription = TestTag.TAG_BUTTON_MOREOPTIONS
                                            }
                                    )
                                }
                                DropdownMenuForResponse(showMenu, homeViewModel, news,{showMenu = false}, listState , onDelete, onNavigateToCreatePost)
                            }
                        }
                    }
                    ExpandableText(news.message)
                    if(news.image.isNotEmpty()){
                        CompositionLocalProvider(
                            localImageLoaderValue
                        ) {
                            val postImageUrl = news.image.toStorageUrl()
                            AutoSizeImage(
                                postImageUrl,
                                contentDescription = "Image",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(300.dp)
                                    .padding(10.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .clickable {
                                        onNavigateToShowImageScreen(news.image)
                                    }
                                    .testTag(TestTag.TAG_POST_IMAGE)
                                    .semantics{
                                        contentDescription = TestTag.TAG_POST_IMAGE
                                    }
                            )
                        }
                    } else {
                        if(news.video.isNotEmpty()) {
                            val videoUri: String = if(news.localPath.isNotEmpty()) {
                                //Load video from local storage
                                getUriStringFromLocalPath(news.localPath)
                            } else {
                                news.video.toStorageUrl()
                            }
                            if(videoUri.isNotEmpty()) {
                                VideoPlayer(videoUri,
                                    Modifier
                                        .fillMaxWidth()
                                        .height(300.dp)
                                        .padding(10.dp)
                                        .testTag(TestTag.TAG_POST_VIDEO)
                                        .semantics{
                                            contentDescription = TestTag.TAG_POST_VIDEO
                                        })
                            }
                        }
                    }
                    if(likeCommentAndShareButtonEnable) {
                        val likeCount = likeCountList[news.id] ?: 0
                        val commentCount = commentCountList[news.id] ?: 0
                        Row(modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 10.dp),
                            verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = if(likeCount > 1) "$likeCount Likes" else "$likeCount Like",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.padding(2.dp)
                            )
                            Spacer(modifier = Modifier.weight(1f))
                            Text(
                                text = if(commentCount > 1) "$commentCount Comments" else "$commentCount Comment",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.padding(2.dp)
                            )
                        }
                        LikeCommentAndShareButton(
                            isLiked,
                            clickLikeButton = {
                                homeViewModel.clickLikeButton(news)
                            },
                            clickCommentButton = {
                                homeViewModel.clickCommentButton(news)
                            },
                            clickShareButton = {
                                //Show bottom sheet
                                showBottomSheet(news)
                            }
                        )
                    }
                }
            }
        }

        @Composable
        fun NewsCardWithSharedContent(
            news: NewsInstance,
            sharedNew : NewsInstance,
            user : UserInstance,
            isLiked : Boolean,
            likeCountList : HashMap<String, Int>,
            commentCountList : HashMap<String, Int>,
            localImageLoaderValue : ProvidedValue<*>,
            onNavigateToShowImageScreen: (image: String) -> Unit,
            onNavigateToUserInformation: (user: UserInstance) -> Unit,
            homeViewModel: HomeViewModel,
            listState : LazyListState,
            onDelete: (action : String, new : NewsInstance) -> Unit,
            onNavigateToCreatePost : (updateNew : NewsInstance) -> Unit,
            showBottomSheet : (NewsInstance) -> Unit) {
            LaunchedEffect(Unit) {
                homeViewModel.updateLikeStatus()
            }
            val loadedUsers by homeViewModel.loadedUserState.collectAsState()
            LaunchedEffect(sharedNew.posterId) {
                homeViewModel.ensureUserLoaded(sharedNew.posterId)
            }
            val ownerUser = loadedUsers[sharedNew.posterId]
            Card(
                modifier = Modifier
                    .padding(start = 10.dp, end = 10.dp, top = 10.dp)
                    .fillMaxWidth()
                    .testTag(TestTag.TAG_POST_IN_COLUMN)
                    .semantics{
                        contentDescription = TestTag.TAG_POST_IN_COLUMN
                    },
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(1.dp)) {
                    Row(horizontalArrangement = Arrangement.Start,
                        modifier = Modifier.background(color = MaterialTheme.colorScheme.surface).padding(10.dp).fillMaxWidth()
                            .clickable {
                                onNavigateToUserInformation(user)
                            }){
                        CompositionLocalProvider(
                            localImageLoaderValue
                        ) {
                            val avatarUrl2 = news.avatar.toStorageUrl()
                            AutoSizeImage(
                                avatarUrl2,
                                contentDescription = "Poster Avatar",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .testTag(TestTag.TAG_POSTER_AVATAR)
                                    .semantics{
                                        contentDescription = TestTag.TAG_POSTER_AVATAR
                                    }
                            )
                        }

                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = news.posterName,
                                color = MaterialTheme.colorScheme.onSurface,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 2.dp)
                            )
                            Text(
                                text = convertTimeToDateString(news.timePosted),
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(horizontal = 2.dp)
                            )
                        }
                        Spacer(modifier = Modifier.weight(1f))
                        //Box to add three dot icon and dropdownMenu when clicking the icon
                        Box{
                            var showMenu by remember { mutableStateOf(false) }
                            IconButton(onClick = {
                                showMenu = true
                            }) {
                                CrossPlatformIcon(
                                    icon = "more_horiz",
                                    backgroundColor = MaterialTheme.colorScheme.surface.toHex(),
                                    contentDescription = "More Options",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier
                                        .testTag(TestTag.TAG_BUTTON_MOREOPTIONS)
                                        .semantics {
                                            contentDescription = TestTag.TAG_BUTTON_MOREOPTIONS
                                        }
                                )
                            }
                            DropdownMenuForResponse(showMenu, homeViewModel, news,{showMenu = false}, listState , onDelete, onNavigateToCreatePost)
                        }
                    }
                    //Message
                    if(news.message.isNotEmpty()) {
                        ExpandableText(news.message)
                    }
                    if(ownerUser != null) {
                        //Shared content
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(10.dp)
                                .border(1.dp, MaterialTheme.colorScheme.outline)
                        ) {
                            NewsCard(
                                sharedNew,
                                ownerUser,
                                isLiked,
                                likeCountList,
                                commentCountList,
                                localImageLoaderValue,
                                likeCommentAndShareButtonEnable = false,
                                hasDropdownMenu = false,
                                onNavigateToShowImageScreen,
                                onNavigateToUserInformation,
                                homeViewModel,
                                listState,
                                onDelete,
                                onNavigateToCreatePost,
                                showBottomSheet
                            )
                        }
                    } else {
                        ShareContentPlaceholder()
                    }
                    //Like and comment part
                    Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 10.dp),
                        horizontalArrangement = Arrangement.Start) {
                        val likeCount = likeCountList[news.id] ?: 0
                        val commentCount = commentCountList[news.id] ?: 0
                        Text(
                            text = if(likeCount > 1) "$likeCount Likes" else "$likeCount Like",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.padding(2.dp)
                        )
                        Spacer(modifier = Modifier.weight(1f))
                        Text(
                            text = if(commentCount > 1) "$commentCount Comments" else "$commentCount Comment",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.padding(2.dp)
                        )
                    }

                    LikeCommentAndShareButton(
                        isLiked,
                        clickLikeButton = {
                            homeViewModel.clickLikeButton(news)
                        },
                        clickCommentButton = {
                            homeViewModel.clickCommentButton(news)
                        },
                        clickShareButton = {
                            //Show bottom sheet
                            showBottomSheet(news)
                        }
                    )
                }
            }
        }

        @Composable
        fun ShowDiscardDialog(
            title: String,
            message: String,
            icon : ImageVector,
            iconBackground : Color,
            onDiscard: () -> Unit,
            onCancel: () -> Unit = {},
            showDialog: MutableState<Boolean>
        ) {
            if (!showDialog.value) return

            Dialog(
                onDismissRequest = { showDialog.value = false }
            ) {
                Card(
                    shape = RoundedCornerShape(28.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .padding(horizontal = 24.dp, vertical = 32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {

                        // Icon Circle
                        Box(
                            modifier = Modifier
                                .size(72.dp)
                                .clip(CircleShape)
                                .background(iconBackground),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = icon,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.error,
                                modifier = Modifier.size(36.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(24.dp))

                        // Title
                        Text(
                            text = title,
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Bold
                            ),
                            textAlign = TextAlign.Center
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        // Message
                        Text(
                            text = message,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center
                        )

                        Spacer(modifier = Modifier.height(28.dp))

                        // Discard Button
                        Button(
                            onClick = {
                                showDialog.value = false
                                onDiscard()
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp)
                                .shadow(8.dp, RoundedCornerShape(16.dp)),
                            shape = RoundedCornerShape(16.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.error
                            )
                        ) {
                            Text(
                                "Discard",
                                color = MaterialTheme.colorScheme.onError,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Cancel Button
                        TextButton(
                            onClick = {
                                showDialog.value = false
                                onCancel() },
                            modifier = Modifier
                                .testTag(TestTag.TAG_BUTTON_NO)
                        ) {
                            Text(
                                "Cancel",
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            }
        }

        @OptIn(ExperimentalMaterial3Api::class)
        @Composable
        fun LogoutBottomSheet(
            onClickConfirm: () -> Unit,
            onNavigateToSignIn: () -> Unit,
            showSheet: MutableState<Boolean>
        ) {
            val coroutineScope = rememberCoroutineScope()
            val sheetState = rememberModalBottomSheetState(
                skipPartiallyExpanded = true
            )

            if (showSheet.value) {

                ModalBottomSheet(
                    onDismissRequest = { showSheet.value = false },
                    sheetState = sheetState,
                    shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
                    containerColor = MaterialTheme.colorScheme.surface
                ) {

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 24.dp, vertical = 16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {

                        Spacer(modifier = Modifier.height(12.dp))

                        // Icon Circle
                        Box(
                            modifier = Modifier
                                .size(80.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.errorContainer),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ExitToApp,
                                contentDescription = "Logout Icon",
                                tint = MaterialTheme.colorScheme.error,
                                modifier = Modifier.size(36.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Text(
                            text = "Logout",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = "Are you sure you want to log out? You can always log back in later.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center
                        )

                        Spacer(modifier = Modifier.height(24.dp))

                        // Log Out Button
                        Button(
                            onClick = {
                                onClickConfirm()
                                showSheet.value = false
                                coroutineScope.launch {
                                    delay(200)
                                    onNavigateToSignIn()
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp),
                            shape = RoundedCornerShape(16.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.error
                            )
                        ) {
                            Text(
                                text = "Log Out",
                                color = MaterialTheme.colorScheme.onError,
                                style = MaterialTheme.typography.titleMedium
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Cancel Button
                        OutlinedButton(
                            onClick = { showSheet.value = false },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Text(
                                text = "Cancel",
                                style = MaterialTheme.typography.titleMedium
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))
                    }
                }
            }
        }

        @Composable
        fun ShowBasicAlertDialog(
            title : String,
            text : String,
            onClickConfirm: () -> Unit,
            onClickReject: () -> Unit,
            showDialog: MutableState<Boolean>
        ) {
            if (showDialog.value) {
                AlertDialog(
                    onDismissRequest = { showDialog.value = false },
                    title = { Text(title) },
                    text = { Text(text) },
                    confirmButton = {
                        Button(onClick = {
                            onClickConfirm()
                            showDialog.value = false
                        }) {
                            Text("Yes")
                        }
                    },
                    dismissButton = {
                        Button(onClick = {
                            showDialog.value = false
                            onClickReject()
                        }) {
                            Text("No")
                        }
                    }
                )
            }
        }


        @Composable
        fun BottomNavigationBar(
            currentRoute: String?,
            onNavigate: (String) -> Unit,
            homeViewModel: HomeViewModel,
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
                            val notificationCount = homeViewModel.listNotificationOfCurrentUser.filter {
                                !it.beRead
                            }.size

                            val showBadge = screen.route == Notification.getScreenName() && notificationCount > 0
                            val testTag = when(screen.route) {
                                Notification.getScreenName() -> TestTag.TAG_NOTIFICATION_BOTTOM
                                Home.getScreenName() -> TestTag.TAG_HOME_BOTTOM
                                Friend.getScreenName() -> TestTag.TAG_FRIEND_BOTTOM
                                Settings.getScreenName() -> TestTag.TAG_SETTING_BOTTOM
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
                            .testTag(TestTag.TAG_BOTTOM_ACTION_BUTTON)
                            .semantics{
                                contentDescription = TestTag.TAG_BOTTOM_ACTION_BUTTON
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
        @Composable
        fun BackAndMoreOptionsRow(navigateBack : () -> Unit) {
            Row(horizontalArrangement = Arrangement.Start,
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surface)
                    .padding(10.dp)){
                CrossPlatformIcon(
                    icon = "arrow_back",
                    backgroundColor = MaterialTheme.colorScheme.surface.toHex(),
                    contentDescription = "Back",
                    tint = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier
                        .size(30.dp)
                        .clip(CircleShape)
                        .testTag(TestTag.TAG_BUTTON_BACK)
                        .semantics{
                            contentDescription = TestTag.TAG_BUTTON_BACK
                        }
                        .clickable {
                            // Handle back button click
                            navigateBack()
                        }
                )
                Spacer(modifier = Modifier.weight(1f))
                CrossPlatformIcon(
                    icon = "more_horiz",
                    backgroundColor = MaterialTheme.colorScheme.surface.toHex(),
                    contentDescription = "More Options",
                    tint = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier
                        .size(30.dp)
                        .clip(CircleShape)
                        .testTag(TestTag.TAG_BUTTON_MOREOPTIONS)
                        .semantics{
                            contentDescription = TestTag.TAG_BUTTON_MOREOPTIONS
                        }
                        .clickable {

                        }
                )
            }
        }

        @Composable
        fun BackAndTitleAndMoreOptionsRow(
            title: String,
            titleColor: Color = Color.Unspecified,
            titleStyle: TextStyle = MaterialTheme.typography.titleMedium,
            subTitle: String = "",
            trailingIcon: String = "",
            trailingIconTint: Color = Color.Unspecified,
            showMoreOptionsMenu: Boolean = false,
            showBackButton: Boolean = true,
            isMember: Boolean = true,
            isAdmin: Boolean = false,
            iconSize: Dp = 35.dp,
            navigateBack: () -> Unit = {},
            onClickMoreOptions: () -> Unit = {},
            onDismissRequest: () -> Unit = {},
            onLeaveGroup: () -> Unit = {},
            onManageMembers: () -> Unit = {}
        ) {

            val sideSlotWidth = iconSize + 16.dp

            val resolvedTitleColor =
                if (titleColor == Color.Unspecified)
                    MaterialTheme.colorScheme.onSurface
                else titleColor

            val resolvedTrailingIconTint =
                if (trailingIconTint == Color.Unspecified)
                    MaterialTheme.colorScheme.onSurface
                else trailingIconTint

            val hasTrailingIcon =
                isMember && trailingIcon.isNotEmpty()

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.background)
                    .padding(horizontal = 10.dp, vertical = 10.dp)
            ) {

                // LEFT SLOT
                Box(
                    modifier = Modifier.width(sideSlotWidth),
                    contentAlignment = Alignment.CenterStart
                ) {

                    if (showBackButton) {

                        CrossPlatformIcon(
                            icon = "arrow_back",
                            backgroundColor = MaterialTheme.colorScheme.surface.toHex(),
                            contentDescription = "Back",
                            tint = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier
                                .size(iconSize)
                                .clip(CircleShape)
                                .clickable {
                                    navigateBack()
                                }
                                .padding(4.dp)
                                .testTag(TestTag.TAG_BUTTON_BACK)
                                .semantics {
                                    contentDescription =
                                        TestTag.TAG_BUTTON_BACK
                                }
                        )
                    }
                }

                // CENTER TITLE
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 8.dp)
                ) {

                    Text(
                        text = title,
                        color = resolvedTitleColor,
                        fontWeight = FontWeight.Bold,
                        style = titleStyle,
                        textAlign = TextAlign.Center,
                        maxLines = 1
                    )

                    if (subTitle.isNotEmpty()) {

                        Text(
                            text = subTitle,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            style = MaterialTheme.typography.bodyMedium,
                            textAlign = TextAlign.Center,
                            maxLines = 1
                        )
                    }
                }

                // RIGHT SLOT
                Box(
                    modifier = Modifier.width(sideSlotWidth),
                    contentAlignment = Alignment.CenterEnd
                ) {

                    if (hasTrailingIcon) {

                        CrossPlatformIcon(
                            icon = trailingIcon,
                            backgroundColor = MaterialTheme.colorScheme.surface.toHex(),
                            contentDescription = "More Options",
                            tint = resolvedTrailingIconTint,
                            modifier = Modifier
                                .size(iconSize)
                                .clip(CircleShape)
                                .clickable {
                                    onClickMoreOptions()
                                }
                                .padding(4.dp)
                                .testTag(TestTag.TAG_BUTTON_MOREOPTIONS)
                                .semantics {
                                    contentDescription =
                                        TestTag.TAG_BUTTON_MOREOPTIONS
                                }
                        )
                    }

                    if (isMember && showMoreOptionsMenu) {

                        DropdownMenuForMoreOptionsInGroup(
                            expanded = showMoreOptionsMenu,
                            isAdmin = isAdmin,
                            onLeaveGroup = onLeaveGroup,
                            onDismissRequest = onDismissRequest,
                            onManageMembers = onManageMembers
                        )
                    }
                }
            }
        }

        @Composable
        fun TabLayout(
            listState: LazyListState,
            tabTitles : List<String>,
            localImageLoaderValue : ProvidedValue<*>,
            homeViewModel: HomeViewModel,
            searchViewModel: SearchViewModel,
            onNavigateToShowImageScreen: (image: String) -> Unit,
            onNavigateToUserInformation: (user: UserInstance?) -> Unit,
            onNavigateToUploadNewsfeed : (updateNew : NewsInstance?) -> Unit){
            var selectedTabIndex by remember { mutableIntStateOf(0) }
            var showBottomSheet by rememberSaveable { mutableStateOf(false) }
            var newToBeShared by mutableStateOf<NewsInstance?>(null)
            Box(modifier = Modifier.fillMaxSize()) {
                Column(modifier = Modifier.fillMaxSize()){
                    TabRow(
                        selectedTabIndex = selectedTabIndex,
                        containerColor = MaterialTheme.colorScheme.background,
                        contentColor = MaterialTheme.colorScheme.onSurface,
                        indicator = {
                                tabPositions ->
                            TabRowDefaults.Indicator(
                                Modifier.tabIndicatorOffset(tabPositions[selectedTabIndex]),
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    ) {
                        tabTitles.forEachIndexed{
                                index, title ->
                            Tab(
                                selected = selectedTabIndex == index,
                                onClick = {
                                    selectedTabIndex = index
                                },
                                text = {
                                    Text(
                                        text = title,
                                        style = MaterialTheme.typography.titleMedium,
                                        color = if(selectedTabIndex == index) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            )
                        }
                    }
                    when(selectedTabIndex){
                        0 -> {
                            if(searchViewModel.query.isNotEmpty()) {
                                var searchList by remember { mutableStateOf<List<UserInstance>>(emptyList()) }
                                // Filtered List
                                LaunchedEffect(searchViewModel.query) {
                                    searchList = homeViewModel.searchUserByName(searchViewModel.query)
                                }
                                LazyColumn(modifier = Modifier
                                    .testTag(TestTag.TAG_PEOPLE_COLUMN)
                                    .semantics {
                                        contentDescription = TestTag.TAG_PEOPLE_COLUMN
                                    }
                                ) {
                                    items(searchList){user ->
                                        SearchUserCard(
                                            user,
                                            localImageLoaderValue,
                                            onClickViewProfileButton = {
                                                onNavigateToUserInformation(user)
                                            }
                                        )
                                    }
                                }
                            } else {
                                Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center,
                                    modifier = Modifier.fillMaxSize()){
                                    Text(text = "Please input person you want to search",
                                        textAlign = TextAlign.Center)
                                }
                            }
                        }
                        1 -> {
                            if(searchViewModel.query.isNotEmpty()) {
                                val filterList by remember {
                                    derivedStateOf {
                                        homeViewModel.listNews.filter { news ->
                                            news.message.contains(searchViewModel.query, ignoreCase = true)
                                        }
                                    }
                                }
                                LazyColumnOfNewsWithSlideOutAnimationAndLoadMore(
                                    localImageLoaderValue,
                                    listState,
                                    homeViewModel,
                                    filterList,
                                    onNavigateToUploadNewsfeed,
                                    onNavigateToShowImageScreen,
                                    onNavigateToUserInformation,
                                    showBottomSheet = { news ->
                                        newToBeShared = news
                                        showBottomSheet = true
                                    })
                            } else {
                                Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center,
                                    modifier = Modifier.fillMaxSize()){
                                    Text(text = "Please input content you want to search",
                                        textAlign = TextAlign.Center)
                                }
                            }
                        }
                    }
                }
                if(showBottomSheet) {
                    ShareBottomSheet(
                        deepLink = "https://firechat-aa433.web.app/news/${newToBeShared?.id}",
                        onDismiss = {
                            showBottomSheet = false
                        },
                        onClick = {
                            showBottomSheet = false
                        }
                    )
                }
            }
        }

        @Composable
        fun UserRow(user : UserInstance,
                    localImageLoaderValue : ProvidedValue<*>,
                    onNavigateToUserInformation: (user: UserInstance) -> Unit) {
            Row(horizontalArrangement = Arrangement.Start,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        onNavigateToUserInformation(user)
                    }
                    .testTag(TestTag.TAG_FRIEND)
                    .semantics{
                        contentDescription = TestTag.TAG_FRIEND
                    }){
                CompositionLocalProvider(
                    localImageLoaderValue
                ) {
                    AutoSizeImage(
                        user.image.toStorageUrl(),
                        contentDescription = "Avatar",
                        contentScale = ContentScale.Crop,
                        modifier =  Modifier
                            .size(60.dp)
                            .padding(10.dp)
                            .clip(CircleShape)
                    )
                }
                Text(
                    text = user.name,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(end = 5.dp), // Adds padding around text
                    maxLines = 2
                )
            }
        }

        @Composable
        fun FriendRequest(localImageLoaderValue : ProvidedValue<*>,
                          requester : UserInstance,
                          currentUser : UserInstance,
                          onNavigateToUserInformation: (user: UserInstance) -> Unit,
                          friendViewModel: FriendViewModel) {
            Column(
                verticalArrangement = Arrangement.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(10.dp)
            ) {
                Row(horizontalArrangement = Arrangement.Start,
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            onNavigateToUserInformation(requester)
                        }
                        .testTag(TestTag.TAG_FRIEND_REQUEST)
                        .semantics{
                            contentDescription = TestTag.TAG_FRIEND_REQUEST
                        }){
                    CompositionLocalProvider(
                        localImageLoaderValue
                    ) {
                        AutoSizeImage(
                            requester.image.toStorageUrl(),
                            contentDescription = "Avatar",
                            contentScale = ContentScale.Crop,
                            modifier =  Modifier
                                .size(80.dp)
                                .padding(10.dp)
                                .clip(CircleShape)
                        )
                    }
                    Column(modifier = Modifier.padding(end = 5.dp)) {
                        Text(
                            text = requester.name,
                            color = MaterialTheme.colorScheme.onSurface,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier
                                .padding(5.dp)
                        )
                    }
                }
                Row(horizontalArrangement = Arrangement.Center,
                    modifier = Modifier
                        .fillMaxWidth()){
                    Button(onClick = {
                        friendViewModel.acceptFriendRequest(requester, currentUser)
                    },
                        elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.onPrimary
                        ),
                        modifier = Modifier
                            .weight(1f)
                    ){
                        Text(text = "Accept")
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Button(onClick = {
                        friendViewModel.rejectFriendRequest(requester, currentUser)
                    },
                        elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant,
                            contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                        ),
                        modifier = Modifier
                            .weight(1f)){
                        Text(text = "Decline")
                    }
                }
            }
        }

        @Composable
        fun ExpandableText(
            text: String,
            collapsedMaxLines: Int = 3
        ) {
            var isExpanded by remember { mutableStateOf(false) }
            var isOverflowing by remember { mutableStateOf(false) }

            Box(modifier = Modifier.padding(horizontal = 10.dp)) {
                Column {
                    Text(
                        text = text,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = if (isExpanded) Int.MAX_VALUE else collapsedMaxLines,
                        overflow = TextOverflow.Ellipsis,
                        onTextLayout = { textLayoutResult ->
                            isOverflowing = textLayoutResult.hasVisualOverflow
                        },
                        modifier = Modifier.animateContentSize()
                    )

                    if (isOverflowing || isExpanded) {
                        Text(
                            text = if (isExpanded) "See less" else "See more",
                            color = MaterialTheme.colorScheme.primary,
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier
                                .padding(top = 4.dp)
                                .clickable { isExpanded = !isExpanded }
                        )
                    }
                }
            }
        }

        @Composable
        fun DropdownMenuForResponse(expanded : Boolean,
                                    homeViewModel: HomeViewModel,
                                    selectedNew : NewsInstance,
                                    onDismissRequest: () -> Unit,
                                    listState : LazyListState,
                                    onDelete: (action : String, new : NewsInstance) -> Unit?,
                                    onNavigateToCreatePost : (updateNew : NewsInstance) -> Unit) {
            DropdownMenu(
                expanded = expanded,
                onDismissRequest = onDismissRequest
            ) {
                var buttonText = "Hide"
                if(selectedNew.posterId == homeViewModel.currentUser!!.uid) {
                    buttonText = "Delete"
                    DropdownMenuItem(
                        text = { Text("Update") },
                        onClick = {
                            onNavigateToCreatePost(selectedNew)
                            onDismissRequest()
                        }
                    )
                }
                val scope = rememberCoroutineScope()
                DropdownMenuItem(
                    text = { Text(buttonText) },
                    onClick = {
                        scope.launch {
                            val index = listState.firstVisibleItemIndex
                            val offset = listState.firstVisibleItemScrollOffset
                            //Dismiss the menu
                            onDismissRequest()
                            onDelete(buttonText, selectedNew)
                            // Scroll back to where we were
                            listState.scrollToItem(index, offset)
                        }
                    }
                )
            }
        }

        @Composable
        fun LazyColumnOfNewsWithSlideOutAnimationAndLoadMore(
            localImageLoaderValue : ProvidedValue<*>,
            listState: LazyListState,
            homeViewModel: HomeViewModel,
            list : List<NewsInstance>,
            onNavigateToUploadNews: (updateNew : NewsInstance?) -> Unit,
            onNavigateToShowImageScreen: (image : String) -> Unit,
            onNavigateToUserInformation: (user: UserInstance?) -> Unit,
            showBottomSheet: (NewsInstance) -> Unit) {
            val coroutineScope = rememberCoroutineScope()
            val likeStatus by homeViewModel.likedPosts.collectAsState()
            val likeCountList = homeViewModel.likeCountList.collectAsState()
            val commentCountList = homeViewModel.commentCountList.collectAsState()
            val loadedUsers by homeViewModel.loadedUserState.collectAsState()
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.background)
                    .testTag(TestTag.TAG_POSTS_COLUMN)
                    .semantics{
                        contentDescription = TestTag.TAG_POSTS_COLUMN
                    },
                state = listState
            ) {
                items(
                    items = list,
                    key = { it.id }
                ) { news ->
                    var isVisible by remember(news.id) { mutableStateOf(true) }
                    val sharedNewMap by homeViewModel.sharedNewsById.collectAsState()
                    LaunchedEffect(news.shareContentId) {
                        homeViewModel.ensureSharedNew(news.shareContentId)
                    }
                    AnimatedVisibility(
                        visible = isVisible,
                        exit = slideOutHorizontally(
                            targetOffsetX = { fullWidth -> fullWidth },
                            animationSpec = tween(durationMillis = 200)
                        )
                    ) {
                        val user = loadedUsers[news.posterId]
                        if(user != null) {
                            if(news.shareContentId.isEmpty()) {
                                NewsCard(
                                    news = news,
                                    user = user,
                                    isLiked = likeStatus.containsKey(news.id),
                                    likeCountList.value,
                                    commentCountList.value,
                                    localImageLoaderValue,
                                    likeCommentAndShareButtonEnable = true,
                                    hasDropdownMenu = true,
                                    onNavigateToShowImageScreen = onNavigateToShowImageScreen,
                                    onNavigateToUserInformation = onNavigateToUserInformation,
                                    homeViewModel = homeViewModel,
                                    listState = listState,
                                    onDelete = { action, deletedNews ->
                                        isVisible = false
                                        coroutineScope.launch {
                                            delay(250)
                                            homeViewModel.deleteOrHideNew(action, deletedNews)
                                        }
                                    },
                                    onNavigateToUploadNews,
                                    showBottomSheet
                                )
                            } else {
                                val sharedNew = sharedNewMap[news.shareContentId]
                                if(sharedNew != null) {
                                    NewsCardWithSharedContent(
                                        news = news,
                                        sharedNew = sharedNew,
                                        user = user,
                                        isLiked = likeStatus.containsKey(news.id),
                                        likeCountList.value,
                                        commentCountList.value,
                                        localImageLoaderValue,
                                        onNavigateToShowImageScreen = onNavigateToShowImageScreen,
                                        onNavigateToUserInformation = onNavigateToUserInformation,
                                        homeViewModel = homeViewModel,
                                        listState = listState,
                                        onDelete = { action, deletedNews ->
                                            isVisible = false
                                            coroutineScope.launch {
                                                delay(250)
                                                homeViewModel.deleteOrHideNew(action, deletedNews)
                                            }
                                        },
                                        onNavigateToUploadNews,
                                        showBottomSheet
                                    )
                                } else {
                                    NewsCardPlaceholder()
                                }
                            }
                        } else {
                            NewsCardPlaceholder()
                        }
                    }
                }

                // Loading row at the bottom
                if (homeViewModel.isLoadingMore.value) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            ThreeDotsLoading(
                                modifier = Modifier.padding(bottom = 10.dp),
                                dotSize = 10.dp,
                                spaceBetween = 5.dp
                            )
                        }
                    }
                }
            }
        }

        @Composable
        fun NewsCardPlaceholder() {
            Card(
                modifier = Modifier
                    .padding(start = 10.dp, end = 10.dp, top = 5.dp)
                    .fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(MaterialTheme.colorScheme.surface)
                        .padding(10.dp)
                ) {
                    // Header skeleton (avatar + lines)
                    Row(
                        horizontalArrangement = Arrangement.Start,
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.surfaceVariant)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Box(
                                modifier = Modifier
                                    .height(14.dp)
                                    .fillMaxWidth(0.4f)
                                    .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(4.dp))
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Box(
                                modifier = Modifier
                                    .height(12.dp)
                                    .fillMaxWidth(0.3f)
                                    .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(4.dp))
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    // Message placeholder
                    Box(
                        modifier = Modifier
                            .height(14.dp)
                            .fillMaxWidth(0.9f)
                            .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(4.dp))
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    // Media placeholder
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(300.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    // Footer placeholders
                    Row(modifier = Modifier.fillMaxWidth()) {
                        Box(
                            modifier = Modifier
                                .height(12.dp)
                                .fillMaxWidth(0.2f)
                                .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(4.dp))
                        )
                        Spacer(modifier = Modifier.weight(1f))
                        Box(
                            modifier = Modifier
                                .height(12.dp)
                                .fillMaxWidth(0.2f)
                                .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(4.dp))
                        )
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                }
            }
        }

        @Composable
        fun NewsCardUnavailable(
            message: String = "This content is not available"
        ) {
            Card(
                modifier = Modifier
                    .padding(start = 10.dp, end = 10.dp, top = 5.dp)
                    .fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 30.dp, horizontal = 20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {

                    // Icon
                    Icon(
                        imageVector = Icons.Outlined.Block,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(48.dp)
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Title
                    Text(
                        text = message,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }


        @Composable
        fun ThreeDotsLoading(
            modifier: Modifier = Modifier,
            dotSize: Dp = 8.dp,
            dotColor: Color = MaterialTheme.colorScheme.primary,
            spaceBetween: Dp = 4.dp,
            animationDelay: Int = 200
        ) {
            val infiniteTransition = rememberInfiniteTransition(label = "dotsAnimation")
            val delays = listOf(0, animationDelay, animationDelay * 2)

            Row(
                modifier = modifier,
                horizontalArrangement = Arrangement.spacedBy(spaceBetween),
                verticalAlignment = Alignment.CenterVertically
            ) {
                delays.forEach { delayMs ->
                    val scale by infiniteTransition.animateFloat(
                        initialValue = 0.5f,
                        targetValue = 1f,
                        animationSpec = infiniteRepeatable(
                            animation = tween(
                                durationMillis = animationDelay * 2,
                                delayMillis = delayMs,
                                easing = LinearEasing
                            ),
                            repeatMode = RepeatMode.Reverse
                        ),
                        label = "dotScale"
                    )

                    Box(
                        modifier = Modifier
                            .size(dotSize)
                            .scale(scale)
                            .background(dotColor, CircleShape)
                    )
                }
            }
        }

        @Composable
        fun MySnackBarHost(hostState: SnackbarHostState, positive: Boolean?) {
            SnackbarHost(hostState = hostState) { data ->
                val contentColor = when (positive) {
                    true -> MaterialTheme.colorScheme.tertiary
                    false -> MaterialTheme.colorScheme.error
                    null -> MaterialTheme.colorScheme.onSurface
                }
                Snackbar(
                    snackbarData = data,
                    containerColor = MaterialTheme.colorScheme.surface,
                    contentColor = contentColor,
                    dismissActionContentColor = MaterialTheme.colorScheme.onSurface
                )
            }
        }

        @Composable
        fun SimpleNewsCard(
            news: NewsInstance,
            localImageLoaderValue : ProvidedValue<*>,
            modifier: Modifier = Modifier
        ) {
            Card(
                modifier = Modifier
                    .padding(horizontal = 10.dp, vertical = 10.dp)
                    .fillMaxWidth()
                    .testTag(TestTag.TAG_POST_IN_COLUMN)
                    .semantics { contentDescription = TestTag.TAG_POST_IN_COLUMN }
                    .then(modifier),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                elevation = CardDefaults.cardElevation(
                    defaultElevation = 2.dp,
                    pressedElevation = 4.dp
                )
            ) {
                Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(1.dp)) {
                    Row(horizontalArrangement = Arrangement.Start,
                        modifier = Modifier
                            .background(color = MaterialTheme.colorScheme.surface)
                            .padding(10.dp)
                            .fillMaxWidth()){
                        CompositionLocalProvider(
                            localImageLoaderValue
                        ) {
                            AutoSizeImage(
                                news.avatar.toStorageUrl(),
                                contentDescription = "Poster Avatar",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .testTag(TestTag.TAG_POSTER_AVATAR)
                                    .semantics{
                                        contentDescription = TestTag.TAG_POSTER_AVATAR
                                    }
                            )
                        }

                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = news.posterName,
                                color = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.padding(horizontal = 2.dp)
                            )
                            Text(
                                text = convertTimeToDateString(news.timePosted),
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(horizontal = 2.dp)
                            )
                        }
                    }
                    ExpandableText(news.message)
                    if(news.image.isNotEmpty()){
                        CompositionLocalProvider(
                            localImageLoaderValue
                        ) {
                            AutoSizeImage(
                                news.image.toStorageUrl(),
                                contentDescription = "Image",
                                contentScale = ContentScale.Fit,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(200.dp)
                                    .padding(5.dp)
                                    .testTag(TestTag.TAG_POST_IMAGE)
                                    .semantics{
                                        contentDescription = TestTag.TAG_POST_IMAGE
                                    }
                            )
                        }
                    } else {
                        if(news.video.isNotEmpty()) {
                            val videoUri: String = if(news.localPath.isNotEmpty()) {
                                //Load video from local storage
                                getUriStringFromLocalPath(news.localPath)
                            } else {
                                news.video.toStorageUrl()
                            }
                            if(videoUri.isNotEmpty()) {
                                VideoPlayer(
                                    videoUri,
                                    Modifier
                                        .fillMaxWidth()
                                        .height(200.dp)
                                        .padding(5.dp)
                                        .testTag(TestTag.TAG_POST_VIDEO)
                                        .semantics{
                                            contentDescription = TestTag.TAG_POST_VIDEO
                                        })
                            }
                        }
                    }
                }
            }
        }

        @Composable
        fun SimpleNewsCardSlideable(
            news: NewsInstance,
            localImageLoaderValue : ProvidedValue<*>,
            onSelected : (NewsInstance) -> Unit,
            onDelete : () -> Unit
        ) {
            val swipeDistancePx = with(LocalDensity.current) { 70.dp.toPx() }
            var offsetX by remember { mutableFloatStateOf(0f) }
            val animatedOffsetX by animateFloatAsState(targetValue = offsetX)
            val swipeThreshold = -swipeDistancePx / 2

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(IntrinsicSize.Min)
                    .testTag(TestTag.TAG_DRAFT)
                    .semantics {
                        contentDescription = TestTag.TAG_DRAFT
                    }
            ) {
                //Row contains delete button
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(MaterialTheme.colorScheme.surface)
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

                // Foreground content (slidable)
                Box(
                    modifier = Modifier
                        .offset { IntOffset(animatedOffsetX.roundToInt(), 0) }
                        .pointerInput(news.id) {
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
                ) {
                    SimpleNewsCard(
                        news,
                        localImageLoaderValue,
                        modifier = Modifier
                            .clip(RoundedCornerShape(16.dp))
                            .clickable {
                                onSelected(news)
                            }
                    )
                }
            }
        }

        @OptIn(ExperimentalMaterial3Api::class)
        @Composable
        fun ShareBottomSheet(
            deepLink : String,
            onDismiss: () -> Unit,
            onClick: (message : String) -> Unit
        ) {
            var message by remember { mutableStateOf("") }
            ModalBottomSheet(
                onDismissRequest = { onDismiss() },
                sheetState = rememberModalBottomSheetState(),
            ) {
                // Sheet Content
                Column(Modifier.padding(16.dp)) {
                    Text(
                        text = "Share on your personal page",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(Modifier.height(10.dp))
                    OutlinedTextField(
                        value = message,
                        onValueChange = {
                            message = it
                        },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = MaterialTheme.colorScheme.onSurface,
                            unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                            disabledTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                        ),
                        maxLines = 4,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp)
                            .testTag(TestTag.TAG_POST_MESSAGE)
                            .semantics{
                                contentDescription = TestTag.TAG_POST_MESSAGE
                            },
                        label = { Text(text = "Say something...") }
                    )
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                        Button(onClick = { onClick(message) }) {
                            Text("Share")
                        }
                    }
                    Spacer(Modifier.height(10.dp))
                    Text("Or share via other apps:")
                    Spacer(Modifier.height(10.dp))
                    //Show all apps that can handle the shared link
                    ShareAppRow(deepLink)
                    Spacer(Modifier.height(10.dp))
                }
            }
        }
        @Composable
        fun ShareAppRow(
            deepLink : String
        ) {
            var shareAppsList by remember(deepLink) {
                mutableStateOf<List<ShareApp>>(emptyList())
            }
            //Fetch all share apps
            // Run when deepLink changes
            LaunchedEffect(deepLink) {
                shareAppsList = queryShareApps(deepLink)
            }
            if (shareAppsList.isEmpty()) {
                Text("No compatible apps found")
            } else {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier
                        .testTag(TestTag.TAG_SHARE_APPS_ROW)
                        .semantics{
                            contentDescription = TestTag.TAG_SHARE_APPS_ROW
                        }
                ) {
                    items(shareAppsList) { app ->
                        Column(
                            modifier = Modifier
                                .width(72.dp)
                                .clickable {
                                    // Launch the selected app with the deep link
                                    launchShareAppWithDeepLink(app, deepLink)
                                },
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            // App Icon
                            if(app.icon != null) {
                                Image(
                                    bitmap = app.icon.asImageBitmap(),
                                    contentDescription = null,
                                    modifier = Modifier.size(48.dp)
                                )
                            }
                            Spacer(Modifier.height(4.dp))

                            //App name
                            Text(
                                text = app.name,
                                fontSize = 12.sp,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
            }
        }

        @Composable
        fun TitleAndSubTitleBelow(
            title : String,
            subTitle : String = "",
            modifier: Modifier = Modifier,
            textAlign: TextAlign = TextAlign.Center
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp)
            ) {
                Text(
                    text = title,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.titleMedium,
                    textAlign = textAlign,
                    overflow = TextOverflow.Ellipsis,
                    maxLines = 1,

                )
                if(subTitle.isNotEmpty()) {
                    Text(
                        text = subTitle,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        style = MaterialTheme.typography.bodyMedium,
                        textAlign = textAlign,
                        overflow = TextOverflow.Ellipsis,
                        maxLines = 1,
                    )
                }
            }
        }

        @Composable
        fun ShareContentPlaceholder() {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(10.dp),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {

                    // Header: avatar + name
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .shimmerPlaceholder()
                        )

                        Spacer(modifier = Modifier.width(8.dp))

                        Column {
                            Box(
                                modifier = Modifier
                                    .height(14.dp)
                                    .width(120.dp)
                                    .shimmerPlaceholder()
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Box(
                                modifier = Modifier
                                    .height(12.dp)
                                    .width(80.dp)
                                    .shimmerPlaceholder()
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Content text
                    Box(
                        modifier = Modifier
                            .height(16.dp)
                            .fillMaxWidth(0.7f)
                            .shimmerPlaceholder()
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // Image placeholder
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(180.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .shimmerPlaceholder()
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Actions row
                    Row(
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Box(
                            modifier = Modifier
                                .height(24.dp)
                                .width(60.dp)
                                .shimmerPlaceholder()
                        )
                        Box(
                            modifier = Modifier
                                .height(24.dp)
                                .width(80.dp)
                                .shimmerPlaceholder()
                        )
                    }
                }
            }
        }

        fun Modifier.shimmerPlaceholder(): Modifier = composed {
            val shimmer = rememberInfiniteTransition()
            val alpha by shimmer.animateFloat(
                initialValue = 0.3f,
                targetValue = 0.7f,
                animationSpec = infiniteRepeatable(
                    animation = tween(800),
                    repeatMode = RepeatMode.Reverse
                )
            )
            val surfaceVariantColor = MaterialTheme.colorScheme.surfaceVariant
            background(
                color = surfaceVariantColor.copy(alpha = alpha),
                shape = RoundedCornerShape(6.dp)
            )
        }

        @Composable
        fun PasswordVisibilityIcon(passwordVisibility : Boolean,
                                   tint : Color,
                                   backgroundColor : String) {
            val icon = if(passwordVisibility) "visibility" else "visibility_off"
            val descriptionOfIcon = if(passwordVisibility) "Hide password" else "Show password"
            CrossPlatformIcon(
                icon = icon,
                backgroundColor = backgroundColor,
                tint = tint,
                contentDescription = descriptionOfIcon,
                modifier = Modifier
                    .size(30.dp)
                    .padding(4.dp)
            )
        }

        @Composable
        fun IconAndTitle(hasIcon : Boolean = true,
                         hasTitle : Boolean = true,
                         icon : String = "",
                         title : String = "",
                         titleColor : Color = Color.Unspecified,
                         modifier: Modifier = Modifier
        ) {
            val resolvedTitleColor = if (titleColor == Color.Unspecified) MaterialTheme.colorScheme.primary else titleColor
            Row(
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically,
                modifier = modifier
            ) {
                if(hasIcon) {
                    CrossPlatformIcon(
                        icon = "fire_chat_icon",
                        backgroundColor = MaterialTheme.colorScheme.background.toHex(),
                        modifier = Modifier
                            .size(30.dp)
                    )
                }
                if(hasTitle) {
                    Text(
                        text = title,
                        color = resolvedTitleColor,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        @Composable
        fun SubTitle(
            subTitle : String,
            modifier: Modifier = Modifier) {
            Row(
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically,
                modifier = modifier
            ) {
                Text(
                    text = subTitle,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.titleSmall,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        @Composable
        fun TextFieldWithLeadingIcon(
            value : String,
            onValueChange : (String) -> Unit,
            label : String,
            testTag : String
        ) {
            OutlinedTextField(
                value = value,
                onValueChange = {
                    onValueChange(it)
                },
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
                    .testTag(testTag)
                    .semantics {
                        contentDescription = testTag
                    },
                leadingIcon = {
                    Icon(
                        Icons.Default.Person,
                        label
                    )
                },
                label = { Text(text = label) },
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = MaterialTheme.colorScheme.onSurface,
                    unfocusedTextColor = MaterialTheme.colorScheme.onSurface
                ),
                textStyle = TextStyle(MaterialTheme.colorScheme.onSurface)
            )
        }

        @Composable
        fun ActionButton(
            modifier: Modifier = Modifier,
            icon: String,
            text: String,
            onClick: () -> Unit,
            buttonColor : Color,
            backgroundColor: String,
            textColor : Color = Color.Unspecified,
            tint: Color = Color.Unspecified
        ) {
            val resolvedTextColor = if (textColor == Color.Unspecified) MaterialTheme.colorScheme.onSurfaceVariant else textColor
            val resolvedTint = if (tint == Color.Unspecified) MaterialTheme.colorScheme.onSurfaceVariant else tint
            Button(
                onClick = onClick,
                modifier = modifier.height(34.dp),
                shape = RoundedCornerShape(10.dp),
                elevation = ButtonDefaults.buttonElevation(4.dp),
                contentPadding = PaddingValues(horizontal = 6.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = buttonColor
                )
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    CrossPlatformIcon(
                        icon = icon,
                        contentDescription = text,
                        tint = resolvedTint,
                        backgroundColor = backgroundColor,
                        modifier = Modifier.size(16.dp)
                    )

                    Spacer(modifier = Modifier.width(4.dp))

                    Text(
                        text = text,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        fontSize = 12.sp,
                        color = resolvedTextColor
                    )
                }
            }
        }

        @Composable
        fun LikeCommentAndShareButton(
            isLiked : Boolean,
            clickLikeButton : () -> Unit,
            clickCommentButton : () -> Unit,
            clickShareButton : () -> Unit
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 10.dp, start = 10.dp, end = 10.dp)) {
                //Like button
                ActionButton(
                    modifier = Modifier
                        .height(35.dp)
                        .weight(1f)
                        .testTag(TestTag.TAG_BUTTON_LIKE)
                        .semantics{
                            contentDescription = TestTag.TAG_BUTTON_LIKE
                        },
                    icon = "like",
                    text = if(isLiked) "Liked" else "Like",
                    onClick = {
                        clickLikeButton()
                    },
                    buttonColor = MaterialTheme.colorScheme.surface,
                    backgroundColor = MaterialTheme.colorScheme.surface.toHex(),
                    textColor = if(isLiked) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                    tint = if(isLiked) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                )
                //Comment button
                ActionButton(
                    modifier = Modifier
                        .height(35.dp)
                        .weight(1f)
                        .testTag(TestTag.TAG_BUTTON_COMMENT)
                        .semantics{
                            contentDescription = TestTag.TAG_BUTTON_COMMENT
                        },
                    icon = "comment",
                    text = "Comment",
                    onClick = {
                        clickCommentButton()
                    },
                    buttonColor = MaterialTheme.colorScheme.surface,
                    backgroundColor = MaterialTheme.colorScheme.surface.toHex(),
                    tint = MaterialTheme.colorScheme.onSurface
                )
                //Share button
                ActionButton(
                    modifier = Modifier
                        .height(35.dp)
                        .weight(1f)
                        .testTag(TestTag.TAG_BUTTON_SHARE)
                        .semantics{
                            contentDescription = TestTag.TAG_BUTTON_SHARE
                        },
                    icon = "share",
                    text = "Share",
                    onClick = {
                        clickShareButton()
                    },
                    buttonColor = MaterialTheme.colorScheme.surface,
                    backgroundColor = MaterialTheme.colorScheme.surface.toHex(),
                    tint = MaterialTheme.colorScheme.onSurface
                )
            }
        }

        @Composable
        fun SearchUserCard(
            user : UserInstance,
            localImageLoaderValue : ProvidedValue<*>,
            onClickViewProfileButton : () -> Unit,
            modifier: Modifier = Modifier
        ) {
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                ),
                modifier = modifier
                    .fillMaxWidth()
                    .padding(10.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .animateContentSize()
                        .padding(10.dp)
                ) {
                    CompositionLocalProvider(localImageLoaderValue) {
                        AutoSizeImage(
                            user.image.toStorageUrl(),
                            contentDescription = "Avatar",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .size(50.dp)
                                .clip(CircleShape)
                        )
                    }

                    Spacer(Modifier.width(10.dp))

                    Text(
                        text = user.name,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontWeight = FontWeight.Bold,
                        overflow = TextOverflow.Ellipsis,
                        maxLines = 2,
                        modifier = Modifier
                            .weight(1f)
                            .padding(horizontal = 5.dp)
                    )

                    Spacer(Modifier.width(8.dp))

                    OutlinedButton(
                        onClick = onClickViewProfileButton,
                        shape = RoundedCornerShape(20.dp),
                        border = BorderStroke(
                            width = 0.5.dp,
                            color = MaterialTheme.colorScheme.primary
                        ),
                        contentPadding = PaddingValues(
                            horizontal = 18.dp,
                            vertical = 6.dp
                        ),
                        colors = ButtonDefaults.outlinedButtonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.onPrimary
                        ),
                        modifier = Modifier.defaultMinSize(
                            minHeight = 0.dp,
                            minWidth = 0.dp
                        )
                    ) {
                        Text(text = "View", fontSize = 14.sp)
                    }
                }
            }
        }

        @Composable
        fun CustomEditText(
            text: String,
            onTextChange: (String) -> Unit,
            modifier: Modifier,
            placeholder: String,
            keyboardController : SoftwareKeyboardController? = null
        ) {
            Box(
                modifier = modifier
                    .height(45.dp)
                    .background(
                        color = MaterialTheme.colorScheme.surface,
                        shape = RoundedCornerShape(10.dp)
                    )
                    .border(
                        width = 1.dp,
                        color = MaterialTheme.colorScheme.outline,
                        shape = RoundedCornerShape(10.dp)
                    )
                    .padding(horizontal = 12.dp),
                contentAlignment = Alignment.CenterStart
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    BasicTextField(
                        value = text,
                        onValueChange = onTextChange,
                        maxLines = 4,
                        modifier = Modifier
                            .weight(1f)
                            .padding(vertical = 8.dp),
                        textStyle = MaterialTheme.typography.bodyMedium.copy(
                            color = MaterialTheme.colorScheme.onSurface
                        ),
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                        keyboardActions = KeyboardActions(
                            onDone = { keyboardController?.hide() }
                        ),
                        decorationBox = { innerTextField ->
                            if (text.isEmpty()) {
                                Text(
                                    text = placeholder,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            innerTextField()
                        }
                    )
                }
            }
        }

        @Composable
        fun PasswordField(
            label: String,
            value: String,
            onValueChange: (String) -> Unit,
            isVisible: Boolean,
            onToggleVisibility: () -> Unit,
            minHeight: androidx.compose.ui.unit.Dp = 56.dp
        ) {
            Column {
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                OutlinedTextField(
                    value = value,
                    onValueChange = onValueChange,
                    modifier = Modifier
                        .fillMaxWidth()
                        .defaultMinSize(minHeight = minHeight),
                    singleLine = true,
                    visualTransformation = if (isVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    trailingIcon = {
                        IconButton(onClick = onToggleVisibility) {
                            Icon(
                                imageVector = if (isVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                contentDescription = null
                            )
                        }
                    },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                        focusedLabelColor = MaterialTheme.colorScheme.primary
                    )
                )
            }
        }

        @Composable
        fun QuestionTextAndClickableText(
            questionText : String,
            questionTextColor : Color = Color.Unspecified,
            clickableText : String,
            clickableTextColor : Color = Color.Unspecified,
            onClick: () -> Unit,
            modifier: Modifier = Modifier
        ) {
            val resolvedQuestionColor = if (questionTextColor == Color.Unspecified) MaterialTheme.colorScheme.onSurface else questionTextColor
            val resolvedClickableColor = if (clickableTextColor == Color.Unspecified) MaterialTheme.colorScheme.onSurface else clickableTextColor
            Row(
                modifier = Modifier
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = questionText,
                    color = resolvedQuestionColor,
                    style = MaterialTheme.typography.bodyMedium
                )

                Spacer(Modifier.width(5.dp))

                Text(
                    text = clickableText,
                    color = resolvedClickableColor,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = modifier
                        .clickable {
                            onClick()
                        }
                )
            }
        }
    }
}