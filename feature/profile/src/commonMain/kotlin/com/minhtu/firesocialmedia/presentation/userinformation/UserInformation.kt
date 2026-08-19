package com.minhtu.firesocialmedia.presentation.userinformation

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Call
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.ProvidedValue
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.minhtu.firesocialmedia.storage.profile.SupabaseStorageProvider
import com.minhtu.firesocialmedia.constants.profile.TestTag
import com.minhtu.firesocialmedia.profile.entity.core.DecentralizationType
import com.minhtu.firesocialmedia.profile.entity.news.NewsInstance
import com.minhtu.firesocialmedia.profile.entity.user.UserInstance
import com.minhtu.firesocialmedia.storage.profile.toStorageUrl
import com.minhtu.firesocialmedia.data.remote.service.imagepicker.profile.ImagePicker
import com.minhtu.firesocialmedia.platform.CommonBackHandler
import com.minhtu.firesocialmedia.platform.getImageBytesFromDrawable
import com.minhtu.firesocialmedia.platform.logMessage
import com.minhtu.firesocialmedia.platform.showToast
import com.minhtu.firesocialmedia.presentation.profile.SessionViewModel
import com.minhtu.firesocialmedia.presentation.profile.EngagementViewModel
import com.minhtu.firesocialmedia.profile.presentation.loading.Loading
import com.minhtu.firesocialmedia.profile.presentation.loading.LoadingViewModel
import com.minhtu.firesocialmedia.utils.profile.TitleBarUtils
import com.minhtu.firesocialmedia.profile.utils.UiUtils
import com.minhtu.firesocialmedia.utils.profile.FeedListUtils
import com.seiko.imageloader.ui.AutoSizeImage
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel

class UserInformation {
    companion object{
        @Composable
        fun UserInformationScreen(
            imagePicker: ImagePicker,
            user : UserInstance?,
            isCurrentUser : Boolean,
            isFriend : Boolean = false,
            paddingValues: PaddingValues,
            localImageLoaderValue : ProvidedValue<*>,
            sessionViewModel: SessionViewModel = koinInject(),
            engagementViewModel: EngagementViewModel = koinInject(),
            friendViewModel: ProfileFriendViewModel = koinInject(),
            userInformationViewModel: UserInformationViewModel,
            onNavigateToShowImageScreen : (image : String) -> Unit,
            onNavigateBack : () -> Unit,
            onNavigateToUploadNewsfeed: (updateNew : NewsInstance?) -> Unit,
            onNavigateToCallingScreen : (user : UserInstance?) -> Unit,
            onNavigateToCommentScreen: (selectedNew : NewsInstance) -> Unit,
        ){
            val loadingViewModel: LoadingViewModel = koinViewModel()
            CommonBackHandler {
                onNavigateBack()
            }

            // Register launcher so imagePicker.pickImage() works
            imagePicker.RegisterLauncher { }

            val isLoading by loadingViewModel.isLoading.collectAsState()
            val coroutineScope = rememberCoroutineScope()
            val addFriendStatus by userInformationViewModel.addFriendStatus.collectAsState()
            var showBottomSheet by rememberSaveable { mutableStateOf(false) }
            var newToBeShared by remember { mutableStateOf<NewsInstance?>(null) }
            val fetchedUser by userInformationViewModel.fetchedUser.collectAsState()
            val currentUser by sessionViewModel.currentUserState.collectAsState()
            var addFriendTimes by rememberSaveable { mutableStateOf(1) }
            var callButtonEnabled by remember { mutableStateOf(true) }
            var showCoverPhotoConfirmDialog by remember { mutableStateOf(false) }
            val backgroundUploadStatus by userInformationViewModel.backgroundUploadStatus.collectAsState()
            // Show confirm dialog whenever a new cover photo is picked
            LaunchedEffect(userInformationViewModel.coverPhoto) {
                if (userInformationViewModel.coverPhoto != SupabaseStorageProvider.DEFAULT_AVATAR_URL && isCurrentUser) {
                    showCoverPhotoConfirmDialog = true
                }
            }
            LaunchedEffect(backgroundUploadStatus) {
                backgroundUploadStatus?.let { success ->
                    showToast(if (success) "Cover photo updated!" else "Failed to update cover photo.")
                    userInformationViewModel.resetBackgroundUploadStatus()
                }
            }
            LaunchedEffect(user, isCurrentUser) {
                if(user != null) {
                    loadingViewModel.showLoading()
                    userInformationViewModel.fetchUserInformation(user.uid, isCurrentUser)
                }
            }
            LaunchedEffect(fetchedUser?.uid) {
                val uid = fetchedUser?.uid ?: return@LaunchedEffect
                logMessage("UserInformationScreen", { "fetchInitialUserNews for $uid" })
                sessionViewModel.ensureUserLoaded(uid)
                userInformationViewModel.fetchInitialUserNews(uid)
            }
            LaunchedEffect(fetchedUser, currentUser) {
                val fetched = fetchedUser ?: return@LaunchedEffect
                val loggedInUser = currentUser ?: return@LaunchedEffect
                loadingViewModel.hideLoading()
                val relationship = userInformationViewModel.checkRelationship(fetched, loggedInUser)
                userInformationViewModel.updateRelationship(relationship)
            }

            LaunchedEffect(currentUser) {
                val loggedInUser = currentUser ?: return@LaunchedEffect
                friendViewModel.updateFriendRequests(loggedInUser.friendRequests)
                friendViewModel.updateFriends(loggedInUser.friends)
            }

            val calleeCurrentState by userInformationViewModel.calleeCurrentState.collectAsState()
            LaunchedEffect(calleeCurrentState) {
                if(calleeCurrentState != null) {
                    if(calleeCurrentState!!) {
                        if(isCurrentUser) {
                            showToast("Cannot call for yourself")
                            // Only re-enable the button when we aren't navigating away
                            callButtonEnabled = true
                        } else {
                            onNavigateToCallingScreen(fetchedUser)
                        }
                    } else {
                        showToast("This user is having another call! Please recall after a few minutes.")
                        // Re-enable when the callee is busy and we stay on this screen
                        callButtonEnabled = true
                    }
                    userInformationViewModel.resetCalleeState()
                }
            }

            val commentStatus by engagementViewModel.commentStatus.collectAsState()
            LaunchedEffect(commentStatus) {
                commentStatus?.let { selectedNew ->
                    onNavigateToCommentScreen(selectedNew)
                    engagementViewModel.resetCommentStatus()
                }
            }

            LaunchedEffect(currentUser) {
                engagementViewModel.seedLikedPosts(currentUser)
            }

            // Preserve scroll position across navigation/back stack using rememberSaveable
            val listState = rememberSaveable(saver = LazyListState.Saver) { LazyListState(0, 0) }
            var isUserInfoVisible by remember { mutableStateOf(true) }
            var userInteracted by remember { mutableStateOf(false) }
            // LaunchedEffect to track the scroll state (hide top bar and show load more)
            LaunchedEffect(listState) {
                snapshotFlow {
                    val layoutInfo = listState.layoutInfo
                    val firstVisible = layoutInfo.visibleItemsInfo.firstOrNull()?.index ?: 0
                    val lastVisible = layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0
                    val totalItems = layoutInfo.totalItemsCount
                    val inProgress = listState.isScrollInProgress

                    Triple(firstVisible, lastVisible, totalItems) to inProgress
                }
                    .distinctUntilChanged()
                    .collectLatest { (triple, state) ->
                        val (firstVisible, lastVisible, totalItems) = triple
                        val inProgress = state
                        if(inProgress && firstVisible > 0) {
                            userInteracted = true
                        }
                        // Show/hide top bar
                        isUserInfoVisible = if(!userInteracted) {
                            true
                        } else {
                            firstVisible == 0
                        }
                        // Trigger load more when near bottom
                        val uid = fetchedUser?.uid
                        if (
                            uid != null &&
                            firstVisible > 0 &&
                            lastVisible >= totalItems - 3 &&
                            !userInformationViewModel.isLoadingMoreUserNews &&
                            userInformationViewModel.hasMoreUserNews
                        ) {
                            userInformationViewModel.loadMoreUserNews(uid)
                        }
                    }
            }

            Box(modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(color = MaterialTheme.colorScheme.background)) {
                Column(
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.Start
                ) {
                    TitleBarUtils.BackAndTitleAndMoreOptionsRow(
                        title = "User Information",
                        titleStyle = MaterialTheme.typography.titleLarge,
                        trailingIcon = "more_horiz",
                        navigateBack = {
                            onNavigateBack()
                        },
                        onClickMoreOptions = {
                        }
                    )
                    //Column contains user info and will be dismissed when scroll down
                    AnimatedVisibility(visible = isUserInfoVisible) {
                        Column {
                            //Cover photo + overlapping avatar in a single Box
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(bottom = 40.dp) // reserve space for avatar overhang
                            ) {
                                var showMenu by remember { mutableStateOf(false) }
                                val coverPhotoModifier = Modifier
                                    .height(160.dp)
                                    .fillMaxWidth()
                                    .clickable { showMenu = true }
                                    .testTag(TestTag.TAG_COVER_PHOTO)
                                    .semantics { contentDescription = TestTag.TAG_COVER_PHOTO }

                                // Cover photo
                                val hasLocalPick = userInformationViewModel.coverPhoto != SupabaseStorageProvider.DEFAULT_AVATAR_URL
                                if (hasLocalPick) {
                                    val imageBytes = produceState<ByteArray?>(initialValue = null, userInformationViewModel.coverPhoto) {
                                        value = imagePicker.loadImageBytes(userInformationViewModel.coverPhoto)
                                    }
                                    if (imageBytes.value != null) {
                                        imagePicker.ByteArrayImage(imageBytes.value, modifier = coverPhotoModifier)
                                    }
                                } else if (userInformationViewModel.uploadedBackgroundUri != null) {
                                    // Show just-uploaded background from local URI
                                    val imageBytes = produceState<ByteArray?>(initialValue = null, userInformationViewModel.uploadedBackgroundUri) {
                                        value = imagePicker.loadImageBytes(userInformationViewModel.uploadedBackgroundUri!!)
                                    }
                                    if (imageBytes.value != null) {
                                        imagePicker.ByteArrayImage(imageBytes.value, modifier = coverPhotoModifier)
                                    }
                                } else {
                                    val remoteBackground = fetchedUser?.background?.takeIf { it.isNotBlank() }
                                    if (remoteBackground != null) {
                                        // Show remote background image
                                        CompositionLocalProvider(localImageLoaderValue) {
                                            AutoSizeImage(
                                                remoteBackground.toStorageUrl(),
                                                contentDescription = "Cover photo",
                                                contentScale = ContentScale.Crop,
                                                modifier = coverPhotoModifier
                                            )
                                        }
                                    } else {
                                        // No background set — show placeholder
                                        val imageBytes = produceState<ByteArray?>(initialValue = null) {
                                            value = getImageBytesFromDrawable("unknownavatar")
                                        }
                                        if (imageBytes.value != null) {
                                            imagePicker.ByteArrayImage(imageBytes.value, modifier = coverPhotoModifier)
                                        }
                                    }
                                }

                                val coverUrlForView = when {
                                    hasLocalPick -> userInformationViewModel.coverPhoto
                                    userInformationViewModel.uploadedBackgroundUri != null -> userInformationViewModel.uploadedBackgroundUri!!
                                    else -> fetchedUser?.background?.takeIf { it.isNotBlank() } ?: ""
                                }
                                UiUtils.DropdownMenuForCoverPhoto(
                                    showMenu, isCurrentUser,
                                    coverUrl = coverUrlForView,
                                    { onNavigateToShowImageScreen(coverUrlForView) },
                                    { imagePicker.pickImage() },
                                    { showMenu = false }
                                )

                                // Avatar pinned to bottom-center, half overlapping cover photo
                                if (fetchedUser != null) {
                                    CompositionLocalProvider(localImageLoaderValue) {
                                        AutoSizeImage(
                                            fetchedUser!!.image.toStorageUrl(),
                                            contentDescription = "image",
                                            contentScale = ContentScale.Crop,
                                            modifier = Modifier
                                                .size(80.dp)
                                                .align(Alignment.BottomCenter)
                                                .offset(y = 40.dp)
                                                .clip(CircleShape)
                                                .border(3.dp, MaterialTheme.colorScheme.background, CircleShape)
                                                .testTag(TestTag.TAG_USER_AVATAR)
                                                .semantics { contentDescription = TestTag.TAG_USER_AVATAR }
                                        )
                                    }
                                }
                            }

                            // Name + status + buttons below cover photo
                            if (fetchedUser != null) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(top = 8.dp, bottom = 8.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    // Name
                                    Text(
                                        text = fetchedUser!!.name,
                                        style = MaterialTheme.typography.titleLarge,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onBackground,
                                        textAlign = TextAlign.Center,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                        modifier = Modifier.padding(horizontal = 48.dp)
                                    )

                                    // Status
                                    if (fetchedUser!!.status.isNotBlank()) {
                                        Spacer(modifier = Modifier.height(2.dp))
                                        var statusExpanded by remember { mutableStateOf(false) }
                                        val statusText = fetchedUser!!.status
                                        val truncated = !statusExpanded && statusText.length > 80
                                        Column(
                                            horizontalAlignment = Alignment.CenterHorizontally,
                                            modifier = Modifier.padding(horizontal = 32.dp)
                                        ) {
                                            Text(
                                                text = if (truncated) "${statusText.take(80)}…" else statusText,
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                textAlign = TextAlign.Center,
                                            )
                                            if (statusText.length > 80) {
                                                Text(
                                                    text = if (statusExpanded) "See less" else "See more",
                                                    style = MaterialTheme.typography.labelSmall,
                                                    color = MaterialTheme.colorScheme.primary,
                                                    modifier = Modifier
                                                        .padding(top = 1.dp)
                                                        .clickable { statusExpanded = !statusExpanded }
                                                )
                                            }
                                        }
                                    }

                                    // Action buttons
                                    if (!isCurrentUser) {
                                        Spacer(modifier = Modifier.height(10.dp))
                                        Row(
                                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Surface(
                                                onClick = {
                                                    if (callButtonEnabled) {
                                                        if(isFriend) {
                                                            callButtonEnabled = false
                                                            coroutineScope.launch {
                                                                val networkStatus = userInformationViewModel.checkInternetConnection()
                                                                if (networkStatus) {
                                                                    userInformationViewModel.checkCalleeAvailable(fetchedUser!!)
                                                                } else {
                                                                    showToast("No internet, please recheck your network!")
                                                                    callButtonEnabled = true
                                                                }
                                                            }
                                                        } else {
                                                            showToast("You can only call your friends!")
                                                        }
                                                    }
                                                },
                                                enabled = callButtonEnabled,
                                                shape = RoundedCornerShape(50),
                                                color = MaterialTheme.colorScheme.secondaryContainer,
                                                shadowElevation = 2.dp,
                                                modifier = Modifier.size(40.dp)
                                            ) {
                                                Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                                                    Icon(
                                                        imageVector = Icons.Default.Call,
                                                        contentDescription = "Call",
                                                        tint = MaterialTheme.colorScheme.onSecondaryContainer,
                                                        modifier = Modifier.size(18.dp)
                                                    )
                                                }
                                            }

                                            var showMenu by remember { mutableStateOf(false) }
                                            Surface(
                                                onClick = {
                                                    val loggedInUser = currentUser
                                                    val fetched = fetchedUser
                                                    if (loggedInUser == null || fetched == null) {
                                                        return@Surface
                                                    }
                                                    coroutineScope.launch {
                                                        val networkStatus = userInformationViewModel.checkInternetConnection()
                                                        if (networkStatus) {
                                                            if (addFriendStatus != Relationship.WAITING_RESPONSE) {
                                                                val relationship = userInformationViewModel.checkRelationship(fetched, loggedInUser)
                                                                userInformationViewModel.updateRelationship(relationship)
                                                                if (relationship == Relationship.NONE && addFriendTimes <= 0) {
                                                                    showToast("You only can add friend once when you go to this page!!!")
                                                                } else {
                                                                    addFriendTimes -= 1
                                                                    userInformationViewModel.clickAddFriendButton(friend = fetched, currentUser = loggedInUser)
                                                                }
                                                            } else {
                                                                showMenu = true
                                                            }
                                                        } else {
                                                            showToast("No internet, please recheck your network!")
                                                        }
                                                    }
                                                },
                                                shape = RoundedCornerShape(50),
                                                color = MaterialTheme.colorScheme.primary,
                                                shadowElevation = 4.dp,
                                                modifier = Modifier.height(40.dp).widthIn(min = 130.dp)
                                            ) {
                                                Box(contentAlignment = Alignment.Center, modifier = Modifier.padding(horizontal = 16.dp)) {
                                                    Text(
                                                        text = when (addFriendStatus) {
                                                            Relationship.FRIEND -> "Unfriend"
                                                            Relationship.FRIEND_REQUEST -> "Cancel Request"
                                                            Relationship.NONE -> "Add Friend"
                                                            Relationship.WAITING_RESPONSE -> "Response"
                                                            else -> "Unknown"
                                                        },
                                                        color = MaterialTheme.colorScheme.onPrimary,
                                                        style = MaterialTheme.typography.labelLarge,
                                                        maxLines = 1
                                                    )
                                                    currentUser?.let { loggedInUser ->
                                                        DropdownMenuForResponse(showMenu, friendViewModel, userInformationViewModel, fetchedUser!!, loggedInUser) { showMenu = false }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    val userNews by userInformationViewModel.userNews.collectAsState()

                    // Home seeds EngagementViewModel's like/comment counts as soon as it fetches
                    // each news item (see HomeViewModel.getLatestNews()). Since UserInformation now
                    // fetches its own news independently instead of reusing Home's already-seeded
                    // list, we must do the same seeding here, otherwise NewsCard's
                    // likeCountList[news.id] ?: 0 / commentCountList[news.id] ?: 0 silently show 0.
                    // Track already-seeded ids so a later recomposition (e.g. after loadMoreUserNews
                    // appends a page) doesn't clobber local optimistic updates from clickLikeButton
                    // or a comment count refreshed after returning from CommentScreen.
                    val seededCountIds = remember { mutableSetOf<String>() }
                    LaunchedEffect(userNews) {
                        userNews.forEach { news ->
                            if (seededCountIds.add(news.id)) {
                                engagementViewModel.addLikeCountData(news.id, news.likeCount)
                                engagementViewModel.addCommentCountData(news.id, news.commentCount)
                            }
                        }
                    }

                    val filterList by remember {
                        derivedStateOf {
                            val user = fetchedUser ?: return@derivedStateOf emptyList()

                            userNews
                                .filter { news ->
                                    news.posterId == user.uid
                                }
                                .filter { news ->
                                    when (news.decentralizationType) {
                                        DecentralizationType.Private ->
                                            news.posterId == currentUser?.uid
                                        else -> true
                                    }
                                }
                                .sortedByDescending { news -> news.timePosted }
                        }
                    }

                    FeedListUtils.LazyColumnOfNewsWithSlideOutAnimationAndLoadMore(
                        localImageLoaderValue,
                        listState,
                        engagementViewModel,
                        sessionViewModel,
                        filterList,
                        { updateNew -> onNavigateToUploadNewsfeed(updateNew) },
                        onNavigateToShowImageScreen,
                        onNavigateToUserInformation = {
                            //Don't allow navigate to this screen again
                        },
                        showBottomSheet = { news ->
                            newToBeShared = news
                            showBottomSheet = true
                        },
                        // This screen pages through userInformationViewModel (loadMoreUserNews),
                        // not engagementViewModel, so pass its own loading flag through so the
                        // three-dots loading row shows the same way it does on Home.
                        isLoadingMore = userInformationViewModel.isLoadingMoreUserNews
                    )
                }
                // Cover photo change confirmation dialog
                if (showCoverPhotoConfirmDialog) {
                    AlertDialog(
                        onDismissRequest = {
                            showCoverPhotoConfirmDialog = false
                            userInformationViewModel.updateCover(SupabaseStorageProvider.DEFAULT_AVATAR_URL) // discard pick
                        },
                        title = { Text("Change Cover Photo") },
                        text = { Text("Are you sure you want to set this as your new cover photo?") },
                        confirmButton = {
                            Button(onClick = {
                                showCoverPhotoConfirmDialog = false
                                if (fetchedUser != null) {
                                    userInformationViewModel.uploadBackground(fetchedUser!!.uid)
                                }
                            }) {
                                Text("Save")
                            }
                        },
                        dismissButton = {
                            TextButton(onClick = {
                                showCoverPhotoConfirmDialog = false
                                userInformationViewModel.updateCover(SupabaseStorageProvider.DEFAULT_AVATAR_URL) // discard pick
                            }) {
                                Text("Cancel")
                            }
                        },
                        shape = RoundedCornerShape(20.dp)
                    )
                }
                if(showBottomSheet) {                    UiUtils.ShareBottomSheet(
                        deepLink = "https://firechat-aa433.web.app/news/${newToBeShared?.id}",
                        onDismiss = {
                            showBottomSheet = false
                        },
                        onClick = {
                            showBottomSheet = false
                        }
                    )
                }
                if(isLoading) {
                    Loading.LoadingScreen()
                }
            }
        }

        @Composable
        fun DropdownMenuForResponse(
                                    expanded : Boolean,
                                    friendViewModel: ProfileFriendViewModel,
                                    userInformationViewModel: UserInformationViewModel,
                                    requester : UserInstance,
                                    currentUser : UserInstance,
                                    onDismissRequest: () -> Unit) {
            DropdownMenu(
                expanded = expanded,
                onDismissRequest = onDismissRequest
            ) {
                DropdownMenuItem(
                    text = { Text("Accept") },
                    onClick = {
                        // Handle Accept action
                        friendViewModel.acceptFriendRequest(requester, currentUser)
                        userInformationViewModel.updateRelationship(Relationship.FRIEND)
                        onDismissRequest()
                    },
                    modifier = Modifier
                        .testTag(TestTag.TAG_ACCEPT_BUTTON)
                        .semantics {
                            contentDescription = TestTag.TAG_ACCEPT_BUTTON
                        }
                )
                DropdownMenuItem(
                    text = { Text("Reject") },
                    onClick = {
                        // Handle Reject action
                        friendViewModel.rejectFriendRequest(requester, currentUser)
                        userInformationViewModel.updateRelationship(Relationship.NONE)
                        onDismissRequest()
                    },
                    modifier = Modifier
                        .testTag(TestTag.TAG_REJECT_BUTTON)
                        .semantics {
                            contentDescription = TestTag.TAG_REJECT_BUTTON
                        }
                )
            }
        }

        fun getScreenName() : String {
            return "UserInformationScreen"
        }
    }
}