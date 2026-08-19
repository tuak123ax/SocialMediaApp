package com.minhtu.firesocialmedia.profile.utils

import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.ProvidedValue
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.minhtu.firesocialmedia.constants.profile.TestTag
import com.minhtu.firesocialmedia.profile.entity.deeplinks.ShareApp
import com.minhtu.firesocialmedia.profile.entity.news.NewsInstance
import com.minhtu.firesocialmedia.profile.entity.user.UserInstance
import com.minhtu.firesocialmedia.platform.CrossPlatformIcon
import com.minhtu.firesocialmedia.profile.platform.VideoPlayer
import com.minhtu.firesocialmedia.platform.convertTimeToDateString
import com.minhtu.firesocialmedia.platform.getUriStringFromLocalPath
import com.minhtu.firesocialmedia.profile.platform.launchShareAppWithDeepLink
import com.minhtu.firesocialmedia.profile.platform.queryShareApps
import com.minhtu.firesocialmedia.platform.toHex
import com.minhtu.firesocialmedia.presentation.profile.SessionViewModel
import com.minhtu.firesocialmedia.presentation.profile.EngagementViewModel
import com.minhtu.firesocialmedia.storage.profile.toStorageUrl
import com.seiko.imageloader.asImageBitmap
import com.seiko.imageloader.ui.AutoSizeImage
import kotlinx.coroutines.launch

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
            engagementViewModel: EngagementViewModel,
            sessionViewModel: SessionViewModel,
            listState : LazyListState,
            onDelete: (action : String, new : NewsInstance) -> Unit,
            onNavigateToCreatePost : (updateNew : NewsInstance) -> Unit,
            showBottomSheet : (NewsInstance) -> Unit) {
            LaunchedEffect(Unit) {
                engagementViewModel.updateLikeStatus()
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
                                DropdownMenuForResponse(showMenu, sessionViewModel, news,{showMenu = false}, listState , onDelete, onNavigateToCreatePost)
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
                                        engagementViewModel.clickLikeButton(news.id, news.posterId, sessionViewModel.currentUser)
                                    },
                                    onCommentClick = {
                                        engagementViewModel.clickCommentButton(news)
                                    },
                                    onShareClick = {
                                        showBottomSheet(news)
                                    },
                                    commentSheetContent = null
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
                                engagementViewModel.clickLikeButton(news.id, news.posterId, sessionViewModel.currentUser)
                            },
                            clickCommentButton = {
                                engagementViewModel.clickCommentButton(news)
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
            engagementViewModel: EngagementViewModel,
            sessionViewModel: SessionViewModel,
            listState : LazyListState,
            onDelete: (action : String, new : NewsInstance) -> Unit,
            onNavigateToCreatePost : (updateNew : NewsInstance) -> Unit,
            showBottomSheet : (NewsInstance) -> Unit) {
            LaunchedEffect(Unit) {
                engagementViewModel.updateLikeStatus()
            }
            val loadedUsers by sessionViewModel.loadedUserState.collectAsState()
            LaunchedEffect(sharedNew.posterId) {
                sessionViewModel.ensureUserLoaded(sharedNew.posterId)
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
                            DropdownMenuForResponse(showMenu, sessionViewModel, news,{showMenu = false}, listState , onDelete, onNavigateToCreatePost)
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
                                engagementViewModel,
                                sessionViewModel,
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
                            engagementViewModel.clickLikeButton(news.id, news.posterId, sessionViewModel.currentUser)
                        },
                        clickCommentButton = {
                            engagementViewModel.clickCommentButton(news)
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
                                    sessionViewModel: SessionViewModel,
                                    selectedNew : NewsInstance,
                                    onDismissRequest: () -> Unit,
                                    listState : LazyListState,
                                    onDelete: (action : String, new : NewsInstance) -> Unit?,
                                    onNavigateToCreatePost : (updateNew : NewsInstance) -> Unit) {
            val currentUser by sessionViewModel.currentUserState.collectAsState()
            DropdownMenu(
                expanded = expanded,
                onDismissRequest = onDismissRequest
            ) {
                var buttonText = "Hide"
                if(selectedNew.posterId == currentUser?.uid) {
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

    }
}
