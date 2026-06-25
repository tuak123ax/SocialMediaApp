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
import androidx.compose.foundation.isSystemInDarkTheme
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
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsBottomHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Poll
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
import com.minhtu.firesocialmedia.data.remote.constant.DataConstant
import com.minhtu.firesocialmedia.core.constants.TestTag
import com.minhtu.firesocialmedia.di.PlatformContext
import com.minhtu.firesocialmedia.core.domain.entity.home.deeplinks.ShareApp
import com.minhtu.firesocialmedia.core.domain.entity.news.NewsInstance
import com.minhtu.firesocialmedia.core.domain.entity.user.UserInstance
import com.minhtu.firesocialmedia.platform.CrossPlatformIcon
import com.minhtu.firesocialmedia.platform.VideoPlayer
import com.minhtu.firesocialmedia.platform.convertTimeToDateString
import com.minhtu.firesocialmedia.platform.getCurrentTime
import com.minhtu.firesocialmedia.platform.getUriStringFromLocalPath
import com.minhtu.firesocialmedia.platform.launchShareAppWithDeepLink
import com.minhtu.firesocialmedia.platform.queryShareApps
import com.minhtu.firesocialmedia.platform.toHex
import com.minhtu.firesocialmedia.presentation.home.HomeViewModelContract
import com.minhtu.firesocialmedia.presentation.navigation.HomeNavGraph
import com.minhtu.firesocialmedia.presentation.comment.CommentScreenApi
import com.minhtu.firesocialmedia.presentation.comment.CommentViewModelContract
import com.minhtu.firesocialmedia.presentation.navigationscreen.Screen
import com.minhtu.firesocialmedia.presentation.navigationscreen.setting.Settings
import com.minhtu.firesocialmedia.presentation.navigationscreen.setting.group.PollViewModelInterface
import com.minhtu.firesocialmedia.core.storage.toStorageUrl
import com.seiko.imageloader.asImageBitmap
import com.seiko.imageloader.ui.AutoSizeImage
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.roundToInt
import org.koin.compose.koinInject

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
            homeViewModel: HomeViewModelContract,
            listState : LazyListState,
            onDelete: (action : String, new : NewsInstance) -> Unit,
            onNavigateToCreatePost : (updateNew : NewsInstance) -> Unit,
            showBottomSheet : (NewsInstance) -> Unit,
            commentViewModel: CommentViewModelContract? = null,
            platform: PlatformContext? = null,
            currentUser: UserInstance? = null) {
            val commentScreenApi: CommentScreenApi = koinInject()
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
                                        },
                                    isLiked,
                                    onLikeClick = {
                                        homeViewModel.clickLikeButton(news)
                                    },
                                    onCommentClick = {
                                        homeViewModel.clickCommentButton(news)
                                    },
                                    onShareClick = {
                                        showBottomSheet(news)
                                    },
                                    commentSheetContent = if (commentViewModel != null && platform != null && currentUser != null) {
                                        { onSheetDismiss ->
                                            commentScreenApi.renderCommentScreen(
                                                paddingValues = PaddingValues(0.dp),
                                                modifier = Modifier,
                                                platform = platform,
                                                localImageLoaderValue = localImageLoaderValue,
                                                showCloseIcon = false,
                                                commentViewModel = commentViewModel,
                                                currentUser = currentUser,
                                                selectedNew = news,
                                                onNavigateToShowImageScreen = onNavigateToShowImageScreen,
                                                onNavigateToUserInformation = { u -> onNavigateToUserInformation(u ?: user) },
                                                onNavigateToHomeScreen = { count ->
                                                    homeViewModel.addCommentCountData(news.id, count)
                                                    onSheetDismiss()
                                                }
                                            )
                                        }
                                    } else null
                                )
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
            homeViewModel: HomeViewModelContract,
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
            val isDarkTheme = isSystemInDarkTheme()
            val shadowAmbient = if (isDarkTheme) Color.White.copy(alpha = 0.08f) else Color.Black
            val shadowSpot = if (isDarkTheme) Color.White.copy(alpha = 0.15f) else Color.Black

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
                                .shadow(8.dp, RoundedCornerShape(16.dp), ambientColor = shadowAmbient, spotColor = shadowSpot),
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
            homeViewModel: HomeViewModelContract,
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

                            val showBadge = screen.route == Screen.Notification.route && notificationCount > 0
                            val testTag = when(screen.route) {
                                Screen.Notification.route -> TestTag.TAG_NOTIFICATION_BOTTOM
                                HomeNavGraph.HOME_SCREEN_NAME -> TestTag.TAG_HOME_BOTTOM
                                Screen.Friend.route -> TestTag.TAG_FRIEND_BOTTOM
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
        fun DropdownMenuForCoverPhoto(
            expanded: Boolean,
            isCurrentUser: Boolean,
            coverUrl: String,
            onViewCoverPhoto: () -> Unit,
            onChangeCoverPhoto: () -> Unit,
            onDismissRequest: () -> Unit
        ) {
            DropdownMenu(expanded = expanded, onDismissRequest = onDismissRequest) {
                if (coverUrl.isNotBlank()) {
                    DropdownMenuItem(
                        text = { Text("View cover photo") },
                        onClick = { onViewCoverPhoto(); onDismissRequest() }
                    )
                }
                if (isCurrentUser) {
                    DropdownMenuItem(
                        text = { Text("Change cover photo") },
                        onClick = { onChangeCoverPhoto(); onDismissRequest() }
                    )
                }
            }
        }

        @Composable
        fun DropdownMenuForResponse(expanded : Boolean,
                                    homeViewModel: HomeViewModelContract,
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
            homeViewModel: HomeViewModelContract,
            list : List<NewsInstance>,
            onNavigateToUploadNews: (updateNew : NewsInstance?) -> Unit,
            onNavigateToShowImageScreen: (image : String) -> Unit,
            onNavigateToUserInformation: (user: UserInstance?) -> Unit,
            showBottomSheet: (NewsInstance) -> Unit,
            commentViewModel: CommentViewModelContract? = null,
            platform: PlatformContext? = null,
            currentUser: UserInstance? = null,
            groupId: String = "",
            pollViewModel: Any? = null,   // PollViewModel — typed as Any? to keep commonMain clean; cast inside
            currentUserId: String = "",
            onDeletePoll: ((NewsInstance) -> Unit)? = null) {
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
                        // Poll entries are invisible to old-app context (no pollViewModel) or home feed (no groupId)
                        val isPollWithoutSupport = news.type == DataConstant.POST_TYPE_POLL && (pollViewModel == null || groupId.isEmpty())
                        if(user != null && !isPollWithoutSupport) {
                            when {
                                news.type == DataConstant.POST_TYPE_POLL && groupId.isNotEmpty() -> {
                                    PollCard(
                                        news = news,
                                        user = user,
                                        localImageLoaderValue = localImageLoaderValue,
                                        homeViewModel = homeViewModel,
                                        currentUserId = currentUserId,
                                        pollViewModel = pollViewModel,
                                        onNavigateToUserInformation = onNavigateToUserInformation,
                                        onDelete = { deletedNews ->
                                            isVisible = false
                                            coroutineScope.launch {
                                                delay(250)
                                                if (onDeletePoll != null) {
                                                    onDeletePoll(deletedNews)
                                                } else {
                                                    homeViewModel.deletePoll(deletedNews, groupId)
                                                }
                                            }
                                        }
                                    )
                                }
                                news.shareContentId.isEmpty() -> {
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
                                    showBottomSheet,
                                    commentViewModel = commentViewModel,
                                    platform = platform,
                                    currentUser = currentUser
                                )
                                }
                                else -> {
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

        /**
         * Renders a poll post card in the feed.
         * Shows the poster header, poll question, answer options with vote bars,
         * and a delete button for the post owner.
         *
         * Full poll detail (options, vote counts) is loaded separately from /polls/{pollId}
         * when needed. This card shows the question and a "View Poll" affordance.
         */
        @Composable
        fun PollCard(
            news: NewsInstance,
            user: UserInstance,
            localImageLoaderValue: ProvidedValue<*>,
            homeViewModel: HomeViewModelContract,
            currentUserId: String = "",
            pollViewModel: Any? = null,
            onNavigateToUserInformation: (UserInstance?) -> Unit,
            onDelete: (NewsInstance) -> Unit
        ) {
            val currentUser = homeViewModel.currentUser
            val vm = pollViewModel as? PollViewModelInterface

            // ── Load full poll data lazily ──
            val pollId = news.pollId ?: ""
            LaunchedEffect(pollId) {
                if (pollId.isNotEmpty() && currentUserId.isNotEmpty()) {
                    vm?.loadPoll(pollId, currentUserId)
                }
            }

            val allPolls by (vm?.polls ?: remember { kotlinx.coroutines.flow.MutableStateFlow(emptyMap<String, com.minhtu.firesocialmedia.core.domain.entity.settings.PollObject>()) }).collectAsState()
            val allMyVotes by (vm?.myVotes ?: remember { kotlinx.coroutines.flow.MutableStateFlow(emptyMap<String, List<Int>>()) }).collectAsState()
            val allSubmitStates by (vm?.submitState ?: remember { kotlinx.coroutines.flow.MutableStateFlow(emptyMap<String, Boolean?>()) }).collectAsState()
            val allVotersMap by (vm?.allVoters ?: remember { kotlinx.coroutines.flow.MutableStateFlow(emptyMap<String, List<Pair<com.minhtu.firesocialmedia.core.domain.entity.user.UserInstance?, List<Int>>>>()) }).collectAsState()

            val poll = allPolls[pollId]
            val myVotes = allMyVotes[pollId]          // null = not loaded yet; empty = loaded, no vote
            val isExpired = poll?.expiresAt?.let { it > 0 && getCurrentTime() > it } ?: false
            val isOwner = currentUser != null && currentUser.uid == news.posterId

            // Load voters when the owner has poll data
            LaunchedEffect(pollId, isOwner, poll) {
                if (isOwner && poll != null) {
                    vm?.loadAllVoters(pollId)
                }
            }

            val votersForPoll = allVotersMap[pollId]
            var showVoterDetails by remember(pollId) { mutableStateOf(false) }
            val hasVoted = myVotes != null && myVotes.isNotEmpty()
            // Show results if: user already voted, or poll is expired, or no options (edge case)
            val showResults = hasVoted || isExpired

            var pendingSelection by remember(pollId) { mutableStateOf<Set<Int>>(emptySet()) }
            val isSubmitting = false // submitState map uses null=idle, so we can't detect in-flight from it

            Card(
                modifier = Modifier
                    .padding(start = 10.dp, end = 10.dp, top = 10.dp)
                    .fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(0.dp)
                ) {
                    // ── Header row ──
                    Row(
                        horizontalArrangement = Arrangement.Start,
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .background(MaterialTheme.colorScheme.surface)
                            .padding(10.dp)
                            .fillMaxWidth()
                            .clickable { onNavigateToUserInformation(user) }
                    ) {
                        CompositionLocalProvider(localImageLoaderValue) {
                            AutoSizeImage(
                                news.avatar.toStorageUrl(),
                                contentDescription = "Poster Avatar",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.size(40.dp).clip(CircleShape)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = news.posterName,
                                color = MaterialTheme.colorScheme.onSurface,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = convertTimeToDateString(news.timePosted),
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontSize = 12.sp
                            )
                        }
                        Spacer(modifier = Modifier.weight(1f))
                        // Poll badge
                        Box(
                            modifier = Modifier
                                .background(MaterialTheme.colorScheme.primaryContainer, RoundedCornerShape(12.dp))
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = if (isExpired) "Closed" else "Poll",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = if (isExpired) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                        // Delete option — only for the post owner
                        if (isOwner) {
                            var showMenu by remember { mutableStateOf(false) }
                            Box {
                                IconButton(onClick = { showMenu = true }) {
                                    CrossPlatformIcon(
                                        icon = "more_horiz",
                                        backgroundColor = MaterialTheme.colorScheme.surface.toHex(),
                                        contentDescription = "More Options",
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                                DropdownMenu(expanded = showMenu, onDismissRequest = { showMenu = false }) {
                                    DropdownMenuItem(
                                        text = { Text("Delete Poll") },
                                        leadingIcon = {
                                            Icon(Icons.Filled.Delete, contentDescription = null, tint = MaterialTheme.colorScheme.error)
                                        },
                                        onClick = { showMenu = false; onDelete(news) }
                                    )
                                }
                            }
                        }
                    }

                    // ── Poll icon + question ──
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    ) {
                        Icon(
                            Icons.Filled.Poll,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(Modifier.width(4.dp))
                        Text("Poll", fontSize = 11.sp, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.SemiBold)
                    }
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = news.message.ifEmpty { poll?.question ?: "" },
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )

                    // ── Expiry info ──
                    val pollExpiresAt = poll?.expiresAt
                    if (pollExpiresAt != null && pollExpiresAt > 0) {
                        val remaining = pollExpiresAt - getCurrentTime()
                        val expiryText = when {
                            isExpired -> "Poll ended"
                            remaining < 60 * 60 * 1000L -> "Ends in <1 hour"
                            remaining < 24 * 60 * 60 * 1000L -> "Ends in ${remaining / (60 * 60 * 1000L)}h"
                            else -> "Ends in ${remaining / (24 * 60 * 60 * 1000L)}d"
                        }
                        Text(
                            text = expiryText,
                            fontSize = 11.sp,
                            color = if (isExpired) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 2.dp)
                        )
                    }

                    Spacer(Modifier.height(8.dp))

                    // ── Options (loading / voting / results) ──
                    if (poll == null) {
                        // Still loading
                        Box(
                            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("Loading poll…", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    } else {
                        val totalVotes = poll.votes?.values?.sum() ?: 0
                        poll.options.forEachIndexed { idx, option ->
                            val voteCount = poll.votes?.get(idx.toString()) ?: 0
                            val fraction = if (totalVotes > 0) voteCount.toFloat() / totalVotes else 0f
                            val isMyVote = myVotes?.contains(idx) == true
                            val isPending = pendingSelection.contains(idx)

                            if (showResults) {
                                // ── Results bar ──
                                Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 3.dp)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = option,
                                            fontSize = 14.sp,
                                            fontWeight = if (isMyVote) FontWeight.Bold else FontWeight.Normal,
                                            color = MaterialTheme.colorScheme.onSurface,
                                            modifier = Modifier.weight(1f)
                                        )
                                        Text(
                                            text = "${(fraction * 100).roundToInt()}%",
                                            fontSize = 12.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                        if (isMyVote) {
                                            Spacer(Modifier.width(4.dp))
                                            Text("✓", fontSize = 12.sp, color = MaterialTheme.colorScheme.primary)
                                        }
                                    }
                                    Spacer(Modifier.height(2.dp))
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(6.dp)
                                            .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(3.dp))
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth(fraction.coerceIn(0f, 1f))
                                                .height(6.dp)
                                                .background(
                                                    if (isMyVote) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondary,
                                                    RoundedCornerShape(3.dp)
                                                )
                                        )
                                    }
                                }
                            } else {
                                // ── Selectable option ──
                                val selected = isPending
                                OutlinedButton(
                                    onClick = {
                                        if (!isExpired && vm != null) {
                                            pendingSelection = if (poll.allowMultipleAnswers) {
                                                if (selected) pendingSelection - idx else pendingSelection + idx
                                            } else {
                                                setOf(idx)
                                            }
                                        }
                                    },
                                    enabled = !isExpired && vm != null,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 16.dp, vertical = 3.dp),
                                    border = BorderStroke(
                                        if (selected) 2.dp else 1.dp,
                                        if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline
                                    ),
                                    colors = ButtonDefaults.outlinedButtonColors(
                                        containerColor = if (selected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface
                                    ),
                                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp)
                                ) {
                                    Text(
                                        text = option,
                                        fontSize = 14.sp,
                                        color = if (selected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface,
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                }
                            }
                        }

                        // ── Vote count + Submit button ──
                        Spacer(Modifier.height(8.dp))
                        Row(
                            modifier = Modifier.padding(horizontal = 16.dp).fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "$totalVotes vote${if (totalVotes != 1) "s" else ""}",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            if (!showResults && pendingSelection.isNotEmpty() && vm != null) {
                                Button(
                                    onClick = {
                                        vm.submitVote(pollId, currentUserId, pendingSelection.sorted())
                                        pendingSelection = emptySet()
                                    },
                                    enabled = !isSubmitting,
                                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 6.dp)
                                ) {
                                    Text("Vote", fontSize = 13.sp)
                                }
                            }
                            // Allow changing vote if already voted and poll still active
                            if (showResults && !isExpired && hasVoted && vm != null) {
                                TextButton(onClick = {
                                    // Clear local vote so the user sees selectable options again
                                    vm.clearMyVote(pollId)
                                    pendingSelection = emptySet()
                                }) {
                                    Text("Change vote", fontSize = 12.sp)
                                }
                            }
                        }

                        // ── Voter details button (poll owner only) ──
                        if (isOwner && poll != null) {
                            Spacer(Modifier.height(4.dp))
                            androidx.compose.material3.HorizontalDivider(
                                modifier = Modifier.padding(horizontal = 16.dp),
                                color = MaterialTheme.colorScheme.outlineVariant
                            )
                            TextButton(
                                onClick = {
                                    // Compare cached voter count against poll vote totals.
                                    // votersForPoll.size = unique voters; totalVotes = sum of per-option counts.
                                    // For single-answer: they should be equal.
                                    // For multi-answer: totalVotes >= votersForPoll.size.
                                    // If the cached total (sum of indices across all voters) differs from
                                    // totalVotes, the cache is stale — refetch.
                                    val cachedVoteTotal = votersForPoll?.sumOf { (_, indices) -> indices.size } ?: -1
                                    if (votersForPoll == null || cachedVoteTotal < totalVotes) {
                                        vm?.refreshAllVoters(pollId)
                                    }
                                    showVoterDetails = true
                                },
                                modifier = Modifier.padding(horizontal = 8.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Person,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(Modifier.width(6.dp))
                                Text("See who voted", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                            }

                            // ── Voters BottomSheet ──
                            if (showVoterDetails) {
                                val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = false)
                                ModalBottomSheet(
                                    onDismissRequest = { showVoterDetails = false },
                                    sheetState = sheetState,
                                    shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
                                    containerColor = MaterialTheme.colorScheme.surface
                                ) {
                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(bottom = 32.dp)
                                    ) {
                                        // Sheet title
                                        Text(
                                            text = "Voters",
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Bold,
                                            modifier = Modifier.padding(horizontal = 20.dp, vertical = 16.dp)
                                        )
                                        androidx.compose.material3.HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

                                        if (votersForPoll == null) {
                                            Box(
                                                modifier = Modifier.fillMaxWidth().padding(vertical = 32.dp),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                androidx.compose.material3.CircularProgressIndicator(modifier = Modifier.size(32.dp))
                                            }
                                        } else if (votersForPoll.isEmpty()) {
                                            Box(
                                                modifier = Modifier.fillMaxWidth().padding(vertical = 32.dp),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Text("No votes yet.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                                            }
                                        } else {
                                            androidx.compose.foundation.lazy.LazyColumn(
                                                modifier = Modifier.fillMaxWidth()
                                            ) {
                                                poll.options.forEachIndexed { idx, option ->
                                                    val votersForOption = votersForPoll.filter { (_, indices) -> indices.contains(idx) }
                                                    if (votersForOption.isNotEmpty()) {
                                                        item {
                                                            // Option header
                                                            Text(
                                                                text = option,
                                                                style = MaterialTheme.typography.labelMedium,
                                                                fontWeight = FontWeight.SemiBold,
                                                                color = MaterialTheme.colorScheme.primary,
                                                                modifier = Modifier
                                                                    .fillMaxWidth()
                                                                    .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f))
                                                                    .padding(horizontal = 20.dp, vertical = 8.dp)
                                                            )
                                                        }
                                                        items(votersForOption) { (user, _) ->
                                                            val displayName = user?.name?.ifEmpty { null } ?: "Unknown"
                                                            val avatarUrl = user?.image?.toStorageUrl() ?: ""
                                                            Row(
                                                                verticalAlignment = Alignment.CenterVertically,
                                                                modifier = Modifier
                                                                    .fillMaxWidth()
                                                                    .padding(horizontal = 20.dp, vertical = 10.dp)
                                                            ) {
                                                                CompositionLocalProvider(localImageLoaderValue) {
                                                                    if (avatarUrl.isNotEmpty()) {
                                                                        AutoSizeImage(
                                                                            avatarUrl,
                                                                            contentDescription = "Avatar",
                                                                            contentScale = ContentScale.Crop,
                                                                            modifier = Modifier
                                                                                .size(40.dp)
                                                                                .clip(CircleShape)
                                                                        )
                                                                    } else {
                                                                        Box(
                                                                            modifier = Modifier
                                                                                .size(40.dp)
                                                                                .clip(CircleShape)
                                                                                .background(MaterialTheme.colorScheme.secondaryContainer),
                                                                            contentAlignment = Alignment.Center
                                                                        ) {
                                                                            Icon(
                                                                                Icons.Filled.Person,
                                                                                contentDescription = null,
                                                                                tint = MaterialTheme.colorScheme.onSecondaryContainer,
                                                                                modifier = Modifier.size(24.dp)
                                                                            )
                                                                        }
                                                                    }
                                                                }
                                                                Spacer(Modifier.width(12.dp))
                                                                Column {
                                                                    Text(
                                                                        text = displayName,
                                                                        style = MaterialTheme.typography.bodyMedium,
                                                                        fontWeight = FontWeight.SemiBold,
                                                                        color = MaterialTheme.colorScheme.onSurface
                                                                    )
                                                                    Text(
                                                                        text = "Voted for: $option",
                                                                        style = MaterialTheme.typography.bodySmall,
                                                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                                                    )
                                                                }
                                                            }
                                                            androidx.compose.material3.HorizontalDivider(
                                                                modifier = Modifier.padding(start = 72.dp, end = 20.dp),
                                                                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                                                            )
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
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
                                        }
                                )
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
                            app.icon?.let { appIcon ->
                                Image(
                                    bitmap = appIcon.asImageBitmap(),
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
            keyboardController : SoftwareKeyboardController? = null,
            focusRequester: FocusRequester? = null
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
                            .padding(vertical = 8.dp)
                            .let { baseMod ->
                                if (focusRequester != null) {
                                    baseMod.focusRequester(focusRequester)
                                } else {
                                    baseMod
                                }
                            },
                        textStyle = MaterialTheme.typography.bodyMedium.copy(
                            color = MaterialTheme.colorScheme.onSurface
                        ),
                        cursorBrush = androidx.compose.ui.graphics.SolidColor(MaterialTheme.colorScheme.primary),
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

        @Composable
        fun DropdownMenuForMoreOptionsInGroup(
            expanded: Boolean,
            isAdmin: Boolean = false,
            onLeaveGroup: () -> Unit,
            onDismissRequest: () -> Unit,
            onManageMembers: () -> Unit
        ) {
            DropdownMenu(
                expanded = expanded,
                onDismissRequest = onDismissRequest
            ) {
                DropdownMenuItem(
                    text = { Text("Leave group") },
                    onClick = {
                        onLeaveGroup()
                        onDismissRequest()
                    }
                )
                if (isAdmin) {
                    DropdownMenuItem(
                        text = { Text("Manage members") },
                        onClick = {
                            onManageMembers()
                            onDismissRequest()
                        }
                    )
                }
            }
        }
    }
}