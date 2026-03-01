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
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.slideOutVertically
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.outlined.Block
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
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
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
import com.minhtu.firesocialmedia.presentation.navigationscreen.setting.group.ManageMembers.Companion.ActionRow
import com.minhtu.firesocialmedia.presentation.search.SearchViewModel
import com.minhtu.sharedmodule.ui.theme.adminBorderColor
import com.minhtu.sharedmodule.ui.theme.adminCardColor
import com.minhtu.sharedmodule.ui.theme.loginBackgroundColor
import com.minhtu.sharedmodule.ui.theme.memberCardColor
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
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(1.dp)) {
                    Row(horizontalArrangement = Arrangement.Start,
                        modifier = Modifier.background(color = Color.White).padding(10.dp).fillMaxWidth()
                            .clickable {
                                onNavigateToUserInformation(user)
                            }){
                        CompositionLocalProvider(
                            localImageLoaderValue
                        ) {
                            AutoSizeImage(
                                news.avatar,
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
                                color = Color.Black,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 2.dp)
                            )
                            Text(
                                text = convertTimeToDateString(news.timePosted),
                                color = Color.Gray,
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
                                        backgroundColor = "#FFFFFFFF",
                                        contentDescription = "More Options",
                                        tint = Color.Gray,
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
                            AutoSizeImage(
                                news.image,
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
                                news.video
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
                                color = Color.Black,
                                modifier = Modifier.padding(2.dp)
                            )
                            Spacer(modifier = Modifier.weight(1f))
                            Text(
                                text = if(commentCount > 1) "$commentCount Comments" else "$commentCount Comment",
                                fontSize = 12.sp,
                                color = Color.Black,
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
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(1.dp)) {
                    Row(horizontalArrangement = Arrangement.Start,
                        modifier = Modifier.background(color = Color.White).padding(10.dp).fillMaxWidth()
                            .clickable {
                                onNavigateToUserInformation(user)
                            }){
                        CompositionLocalProvider(
                            localImageLoaderValue
                        ) {
                            AutoSizeImage(
                                news.avatar,
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
                                color = Color.Black,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 2.dp)
                            )
                            Text(
                                text = convertTimeToDateString(news.timePosted),
                                color = Color.Gray,
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
                                    backgroundColor = "#FFFFFFFF",
                                    contentDescription = "More Options",
                                    tint = Color.Gray,
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
                                .border(1.dp, Color.Black)
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
                            color = Color.Black,
                            modifier = Modifier.padding(2.dp)
                        )
                        Spacer(modifier = Modifier.weight(1f))
                        Text(
                            text = if(commentCount > 1) "$commentCount Comments" else "$commentCount Comment",
                            fontSize = 12.sp,
                            color = Color.Black,
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
        fun ShowAlertDialog(title : String, message : String, resetAndBack:() -> Unit, showDialog : MutableState<Boolean>) {
            if (showDialog.value) {
                AlertDialog(
                    onDismissRequest = { showDialog.value = false },
                    title = { Text(title) },
                    text = { Text(message) },
                    confirmButton = {
                        Button(
                            onClick = {
                            resetAndBack()
                        },
                            modifier = Modifier
                                .testTag(TestTag.TAG_BUTTON_YES)
                                .semantics{
                                    contentDescription = TestTag.TAG_BUTTON_YES
                                }
                            ) {
                            Text("Yes")
                        }
                    },
                    dismissButton = {
                        Button(onClick = { showDialog.value = false },
                            modifier = Modifier.testTag(TestTag.TAG_BUTTON_NO)
                                .semantics{
                                    contentDescription = TestTag.TAG_BUTTON_NO
                                }) {
                            Text("No")
                        }
                    }
                )
            }
        }

        @Composable
        fun ShowAlertDialogToLogout(
            onClickConfirm: () -> Unit,
            onNavigateToSignIn: () -> Unit,
            showDialog: MutableState<Boolean>
        ) {
            val coroutineScope = rememberCoroutineScope()

            if (showDialog.value) {
                AlertDialog(
                    onDismissRequest = { showDialog.value = false },
                    title = { Text("Logout") },
                    text = { Text("Are you sure you want to logout?") },
                    confirmButton = {
                        Button(onClick = {
                            onClickConfirm()
                            showDialog.value = false
                            coroutineScope.launch {
                                delay(100) // let the dialog close properly
                                onNavigateToSignIn()
                            }
                        }) {
                            Text("Yes")
                        }
                    },
                    dismissButton = {
                        Button(onClick = { showDialog.value = false }) {
                            Text("No")
                        }
                    }
                )
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
            modifier: Modifier,
            useDefaultInsets: Boolean = true
        ) {
            val items = listOf(
                Screen.Home,
                Screen.Friend,
                Screen.Notification,
                Screen.Settings
            )
            Box(modifier = modifier){
                val barInsets = if (useDefaultInsets) NavigationBarDefaults.windowInsets else WindowInsets(0)
                NavigationBar(
                    containerColor = Color.White,
                    windowInsets = barInsets
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
                        .offset(y = (-30).dp)
                        .align(Alignment.BottomCenter)
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
                        tint = Color.White
                    )
                }

            }
        }
        @Composable
        fun BackAndMoreOptionsRow(navigateBack : () -> Unit) {
            Row(horizontalArrangement = Arrangement.Start,
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White)
                    .padding(10.dp)){
                CrossPlatformIcon(
                    icon = "arrow_back",
                    backgroundColor = "#FFFFFFFF",
                    contentDescription = "Back",
                    tint = Color.Black,
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
                    backgroundColor = "#FFFFFFFF",
                    contentDescription = "More Options",
                    tint = Color.Black,
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
            title : String,
            titleColor : Color = Color.Black,
            titleStyle : TextStyle = MaterialTheme.typography.titleMedium,
            subTitle : String = "",
            trailingIcon : String = "",
            trailingIconTint : Color = Color.Black,
            showMoreOptionsMenu : Boolean = false,
            showBackButton : Boolean = true,
            isMember : Boolean = true,
            isAdmin : Boolean = false,
            iconSize : Dp = 35.dp,
            navigateBack : () -> Unit = {},
            onClickMoreOptions : () -> Unit = {},
            onDismissRequest : () -> Unit = {},
            onLeaveGroup : () -> Unit = {},
            onManageMembers : () -> Unit = {}) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White)
                    .padding(10.dp)
            ) {
                if(showBackButton) {
                    CrossPlatformIcon(
                        icon = "arrow_back",
                        backgroundColor = "#FFFFFFFF",
                        contentDescription = "Back",
                        tint = Color.Black,
                        modifier = Modifier
                            .size(iconSize)
                            .padding(4.dp)
                            .clip(CircleShape)
                            .testTag(TestTag.TAG_BUTTON_BACK)
                            .semantics { contentDescription = TestTag.TAG_BUTTON_BACK }
                            .clickable { navigateBack() }
                    )
                }

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .padding(horizontal = 8.dp)
                ) {
                    Text(
                        text = title,
                        color = titleColor,
                        fontWeight = FontWeight.Bold,
                        style = titleStyle,
                        textAlign = TextAlign.Center
                    )
                    if(subTitle.isNotEmpty()) {
                        Text(
                            text = subTitle,
                            color = Color.Gray,
                            style = MaterialTheme.typography.bodyMedium,
                            textAlign = TextAlign.Center
                        )
                    }
                }

                if(isMember) {
                    Box{
                        CrossPlatformIcon(
                            icon = trailingIcon,
                            backgroundColor = "#FFFFFFFF",
                            contentDescription = "More Options",
                            tint = trailingIconTint,
                            modifier = Modifier
                                .size(iconSize)
                                .clip(CircleShape)
                                .testTag(TestTag.TAG_BUTTON_MOREOPTIONS)
                                .semantics { contentDescription = TestTag.TAG_BUTTON_MOREOPTIONS }
                                .clickable {
                                    onClickMoreOptions()
                                }
                                .padding(4.dp)
                        )
                        if(showMoreOptionsMenu) {
                            DropdownMenuForMoreOptionsInGroup(
                                showMoreOptionsMenu,
                                isAdmin = isAdmin,
                                onLeaveGroup = {
                                    onLeaveGroup()
                                },
                                onDismissRequest = {
                                    onDismissRequest()
                                },
                                onManageMembers = {
                                    onManageMembers()
                                }
                            )
                        }
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
                        containerColor = Color.White,
                        contentColor = Color.Black,
                        indicator = {
                                tabPositions ->
                            TabRowDefaults.Indicator(
                                Modifier.tabIndicatorOffset(tabPositions[selectedTabIndex]),
                                color = Color.Red
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
                                        color = if(selectedTabIndex == index) Color.Red else Color.Gray
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
                        user.image,
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
                    color = Color.Black,
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
                            requester.image,
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
                            color = Color.Black,
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
                        colors = ButtonDefaults.buttonColors(Color.Red),
                        modifier = Modifier
                            .weight(1f)
                    ){
                        Text(text = "Accept",
                            color = Color.White)
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Button(onClick = {
                        friendViewModel.rejectFriendRequest(requester, currentUser)
                    },
                        elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp),
                        colors = ButtonDefaults.buttonColors(memberCardColor),
                        modifier = Modifier
                            .weight(1f)){
                        Text(text = "Decline", color = Color.Black)
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
                        color = Color.Black,
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
                    .background(Color(0xFFE8E8E8))
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
                                dotColor = Color.Blue,
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
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.White)
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
                                .background(Color(0xFFEAEAEA))
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Box(
                                modifier = Modifier
                                    .height(14.dp)
                                    .fillMaxWidth(0.4f)
                                    .background(Color(0xFFEAEAEA), RoundedCornerShape(4.dp))
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Box(
                                modifier = Modifier
                                    .height(12.dp)
                                    .fillMaxWidth(0.3f)
                                    .background(Color(0xFFF0F0F0), RoundedCornerShape(4.dp))
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    // Message placeholder
                    Box(
                        modifier = Modifier
                            .height(14.dp)
                            .fillMaxWidth(0.9f)
                            .background(Color(0xFFEAEAEA), RoundedCornerShape(4.dp))
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    // Media placeholder
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(300.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0xFFEAEAEA))
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    // Footer placeholders
                    Row(modifier = Modifier.fillMaxWidth()) {
                        Box(
                            modifier = Modifier
                                .height(12.dp)
                                .fillMaxWidth(0.2f)
                                .background(Color(0xFFF0F0F0), RoundedCornerShape(4.dp))
                        )
                        Spacer(modifier = Modifier.weight(1f))
                        Box(
                            modifier = Modifier
                                .height(12.dp)
                                .fillMaxWidth(0.2f)
                                .background(Color(0xFFF0F0F0), RoundedCornerShape(4.dp))
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
                colors = CardDefaults.cardColors(containerColor = Color.White)
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
                        tint = Color(0xFFBDBDBD),
                        modifier = Modifier.size(48.dp)
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Title
                    Text(
                        text = message,
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color(0xFF9E9E9E),
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
                    true -> Color.Green
                    false -> Color.Red
                    null -> MaterialTheme.colorScheme.onSurface
                }
                Snackbar(
                    snackbarData = data,
                    containerColor = Color.White,
                    contentColor = contentColor,
                    dismissActionContentColor = Color.Black
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
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                elevation = CardDefaults.cardElevation(
                    defaultElevation = 2.dp,
                    pressedElevation = 4.dp
                )
            ) {
                Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(1.dp)) {
                    Row(horizontalArrangement = Arrangement.Start,
                        modifier = Modifier
                            .background(color = Color.White)
                            .padding(10.dp)
                            .fillMaxWidth()){
                        CompositionLocalProvider(
                            localImageLoaderValue
                        ) {
                            AutoSizeImage(
                                news.avatar,
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
                                color = Color.Black,
                                modifier = Modifier.padding(horizontal = 2.dp)
                            )
                            Text(
                                text = convertTimeToDateString(news.timePosted),
                                color = Color.Gray,
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
                                news.image,
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
                                news.video
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
                        .background(Color.White)
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
                    Text("Share on your personal page")
                    Spacer(Modifier.height(10.dp))
                    OutlinedTextField(
                        value = message,
                        onValueChange = {
                            message = it
                        },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.Black,
                            unfocusedTextColor = Color.Black,
                            disabledTextColor = Color.Black
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
                    color = Color.Black,
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.titleMedium,
                    textAlign = textAlign,
                    overflow = TextOverflow.Ellipsis,
                    maxLines = 1,

                )
                if(subTitle.isNotEmpty()) {
                    Text(
                        text = subTitle,
                        color = Color.LightGray,
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
                colors = CardDefaults.cardColors(containerColor = Color.White)
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

            background(
                color = Color.LightGray.copy(alpha = alpha),
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
                         titleColor : Color = Color.Red,
                         modifier: Modifier = Modifier
        ) {
            Row(
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically,
                modifier = modifier
            ) {
                if(hasIcon) {
                    CrossPlatformIcon(
                        icon = "fire_chat_icon",
                        backgroundColor = loginBackgroundColor.toHex(),
                        modifier = Modifier
                            .size(30.dp)
                    )
                }
                if(hasTitle) {
                    Text(
                        text = title,
                        color = titleColor,
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
                    color = Color.LightGray,
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
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White
                ),
                textStyle = TextStyle(Color.White)
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
            textColor : Color = Color.Gray,
            tint: Color = Color.Gray
        ) {
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
                        tint = tint,
                        backgroundColor = backgroundColor,
                        modifier = Modifier.size(16.dp)
                    )

                    Spacer(modifier = Modifier.width(4.dp))

                    Text(
                        text = text,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        fontSize = 12.sp,
                        color = textColor
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
                    buttonColor = Color.White,
                    backgroundColor = Color.White.toHex(),
                    textColor = if(isLiked) Color.Red else Color.Gray,
                    tint = if(isLiked) Color.Red else Color.Black
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
                    buttonColor = Color.White,
                    backgroundColor = Color.White.toHex(),
                    tint = Color.Black
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
                    buttonColor = Color.White,
                    backgroundColor = Color.White.toHex(),
                    tint = Color.Black
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
                    containerColor = memberCardColor
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
                            user.image,
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
                        color = Color.Black,
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
                            contentColor = Color.White
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
    }
}