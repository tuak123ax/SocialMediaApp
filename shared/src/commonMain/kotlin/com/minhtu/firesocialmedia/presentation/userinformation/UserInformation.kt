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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Call
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.minhtu.firesocialmedia.constants.Constants
import com.minhtu.firesocialmedia.constants.TestTag
import com.minhtu.firesocialmedia.data.remote.service.imagepicker.ImagePicker
import com.minhtu.firesocialmedia.domain.core.DecentralizationType
import com.minhtu.firesocialmedia.domain.entity.news.NewsInstance
import com.minhtu.firesocialmedia.domain.entity.user.UserInstance
import com.minhtu.firesocialmedia.platform.CommonBackHandler
import com.minhtu.firesocialmedia.platform.getImageBytesFromDrawable
import com.minhtu.firesocialmedia.platform.showToast
import com.minhtu.firesocialmedia.presentation.home.HomeViewModel
import com.minhtu.firesocialmedia.presentation.loading.Loading
import com.minhtu.firesocialmedia.presentation.loading.LoadingViewModel
import com.minhtu.firesocialmedia.presentation.navigationscreen.friend.FriendViewModel
import com.minhtu.firesocialmedia.storage.toStorageUrl
import com.minhtu.firesocialmedia.utils.UiUtils
import com.seiko.imageloader.ui.AutoSizeImage
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch

class UserInformation {
    companion object{
        @Composable
        fun UserInformationScreen(
            imagePicker: ImagePicker,
            user : UserInstance?,
            isCurrentUser : Boolean,
            paddingValues: PaddingValues,
            localImageLoaderValue : ProvidedValue<*>,
            homeViewModel : HomeViewModel,
            friendViewModel: FriendViewModel,
            userInformationViewModel: UserInformationViewModel,
            loadingViewModel: LoadingViewModel,
            onNavigateToShowImageScreen : (image : String) -> Unit,
            onNavigateBack : () -> Unit,
            onNavigateToUploadNewsfeed: (updateNew : NewsInstance?) -> Unit,
            onNavigateToCallingScreen : (user : UserInstance?) -> Unit,
            onNavigateToCommentScreen: (selectedNew : NewsInstance) -> Unit,
        ){
            CommonBackHandler {
                onNavigateBack()
            }
            val isLoading by loadingViewModel.isLoading.collectAsState()
            val coroutineScope = rememberCoroutineScope()
            val newsList = homeViewModel.allNews.collectAsState()
            val addFriendStatus by userInformationViewModel.addFriendStatus.collectAsState()
            var showBottomSheet by rememberSaveable { mutableStateOf(false) }
            var newToBeShared by remember { mutableStateOf<NewsInstance?>(null) }
            val fetchedUser by userInformationViewModel.fetchedUser.collectAsState()
            var addFriendTimes by rememberSaveable { mutableStateOf(1) }
            var callButtonEnabled by remember { mutableStateOf(true) }
            LaunchedEffect(Unit) {
                if(user != null) {
                    loadingViewModel.showLoading()
                    userInformationViewModel.fetchUserInformation(user.uid, isCurrentUser)
                }
            }
            LaunchedEffect(fetchedUser) {
                if(fetchedUser != null) {
                    loadingViewModel.hideLoading()
                    val relationship =
                        userInformationViewModel.checkRelationship(fetchedUser!!, homeViewModel.currentUser!!)
                    userInformationViewModel.updateRelationship(relationship)
                }
            }

            LaunchedEffect(Unit) {
                friendViewModel.updateFriendRequests(homeViewModel.currentUser!!.friendRequests)
                friendViewModel.updateFriends(homeViewModel.currentUser!!.friends)
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

            val commentStatus by homeViewModel.commentStatus.collectAsState()
            LaunchedEffect(commentStatus) {
                commentStatus?.let { selectedNew ->
                    onNavigateToCommentScreen(selectedNew)
                    homeViewModel.resetCommentStatus()
                }
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
                    UiUtils.BackAndTitleAndMoreOptionsRow(
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
                            //Cover photo box
                            Box(contentAlignment = Alignment.Center,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(10.dp)
                                    .clip(RoundedCornerShape(10.dp))) {
                                var showMenu by remember { mutableStateOf(false) }
                                val coverPhotoModifier = Modifier
                                    .height(200.dp)
                                    .fillMaxWidth()
                                    .clickable {
                                        showMenu = true
                                    }
                                    .testTag(TestTag.TAG_COVER_PHOTO)
                                    .semantics {
                                        contentDescription = TestTag.TAG_COVER_PHOTO
                                    }
                                val imageBytes = produceState<ByteArray?>(
                                    initialValue = null,
                                    userInformationViewModel.coverPhoto
                                ) {
                                    value =
                                        if (userInformationViewModel.coverPhoto == Constants.DEFAULT_AVATAR_URL) {
                                            getImageBytesFromDrawable("unknownavatar")
                                        } else {
                                            imagePicker.loadImageBytes(userInformationViewModel.coverPhoto)
                                        }
                                }
                                if (imageBytes.value != null) {
                                    imagePicker.ByteArrayImage(
                                        imageBytes.value,
                                        modifier = coverPhotoModifier
                                    )
                                }
                                DropdownMenuForCoverPhoto(
                                    showMenu,
                                    isCurrentUser,
                                    { onNavigateToShowImageScreen(userInformationViewModel.coverPhoto) },
                                    { imagePicker.pickImage() },
                                    { showMenu = false })
                            }
                            //User avatar, name and button
                            if(fetchedUser != null) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 10.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween // Ensures spacing between name and buttons
                                ) {
                                    Column(
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        modifier = Modifier
                                            .weight(1f)
                                            .offset(y = (-50).dp) // Moves avatar & name up
                                    ) {
                                        // User avatar
                                        CompositionLocalProvider(
                                            localImageLoaderValue
                                        ) {
                                            AutoSizeImage(
                                                fetchedUser!!.image.toStorageUrl(),
                                                contentDescription = "image",
                                                contentScale = ContentScale.Crop,
                                                modifier = Modifier
                                                    .size(100.dp)
                                                    .clip(CircleShape)
                                            .border(
                                                2.dp,
                                                MaterialTheme.colorScheme.surface,
                                                CircleShape
                                            ) // Optional border for better appearance
                                                    .testTag(TestTag.TAG_USER_AVATAR)
                                                    .semantics {
                                                        contentDescription = TestTag.TAG_USER_AVATAR
                                                    }
                                            )
                                        }
                                        Spacer(modifier = Modifier.height(10.dp)) // Space between avatar and name
                                        // User name with max width & ellipsis
                                        Text(
                                            text = fetchedUser!!.name,
                                            color = MaterialTheme.colorScheme.onBackground,
                                            fontSize = 20.sp,
                                            fontWeight = FontWeight.Bold,
                                            textAlign = TextAlign.Center,
                                            modifier = Modifier.widthIn(max = 150.dp), // Restrict width to avoid touching buttons
                                            overflow = TextOverflow.Ellipsis, // Add "..." if too long
                                            maxLines = 1
                                        )
                                    }

                                    // Move buttons up by adjusting offset(y = -20.dp)
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.offset(y = (-20).dp) // Moves buttons up
                                    ) {
                                        // Chat button
//                            IconButton(
//                                onClick = { /* Handle click */ },
//                                modifier = Modifier.Companion.border(
//                                    1.dp,
//                                    Color.Companion.Black,
//                                    CircleShape
//                                )
//                            ) {
//                                Icon(
//                                    imageVector = Icons.Default.Message,
//                                    contentDescription = "Chat",
//                                    tint = Color.Companion.Gray
//                                )
//                            }

                                        //Call button
                                        if(!isCurrentUser) {
                                            IconButton(
                                                enabled = callButtonEnabled,
                                                onClick = {
                                                    if(callButtonEnabled) {
                                                        callButtonEnabled = false
                                                        coroutineScope.launch {
                                                            val networkStatus = userInformationViewModel.checkInternetConnection()
                                                            if(networkStatus) {
                                                                userInformationViewModel.checkCalleeAvailable(fetchedUser!!)
                                                            } else {
                                                                showToast("No internet, please recheck your network!")
                                                                callButtonEnabled = true
                                                            }
                                                        }
                                                    }
                                                },
                                            modifier = Modifier.border(
                                                    1.dp,
                                                    MaterialTheme.colorScheme.outline,
                                                    CircleShape
                                                )
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.Call,
                                                    contentDescription = "Call",
                                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                                )
                                            }

                                            Spacer(modifier = Modifier.width(8.dp)) // Space between buttons

                                            // Add friend button
                                            var showMenu by remember { mutableStateOf(false) }
                                            Surface(
                                                onClick = {
                                                    coroutineScope.launch {
                                                        val networkStatus =
                                                            userInformationViewModel.checkInternetConnection()

                                                        if (networkStatus) {
                                                            if (addFriendStatus != Relationship.WAITING_RESPONSE) {
                                                                val relationship =
                                                                    userInformationViewModel.checkRelationship(
                                                                        fetchedUser!!,
                                                                        homeViewModel.currentUser!!
                                                                    )
                                                                userInformationViewModel.updateRelationship(relationship)

                                                                if (relationship == Relationship.NONE && addFriendTimes <= 0) {
                                                                    showToast("You only can add friend once when you go to this page!!!")
                                                                } else {
                                                                    addFriendTimes -= 1
                                                                    userInformationViewModel.clickAddFriendButton(
                                                                        friend = fetchedUser,
                                                                        currentUser = homeViewModel.currentUser
                                                                    )
                                                                }
                                                            } else {
                                                                showMenu = true
                                                            }
                                                        } else {
                                                            showToast("No internet, please recheck your network!")
                                                        }
                                                    }
                                                },
                                                modifier = Modifier.width(140.dp),
                                                shape = RoundedCornerShape(20.dp),
                                                color = MaterialTheme.colorScheme.primary,
                                                shadowElevation = 6.dp
                                            ) {
                                                Box(
                                                    contentAlignment = Alignment.Center,
                                                    modifier = Modifier.padding(vertical = 10.dp)
                                                ) {
                                                    Text(
                                                        text = when (addFriendStatus) {
                                                            Relationship.FRIEND -> "Unfriend"
                                                            Relationship.FRIEND_REQUEST -> "Cancel Request"
                                                            Relationship.NONE -> "Add Friend"
                                                            Relationship.WAITING_RESPONSE -> "Response"
                                                            else -> "Unknown"
                                                        },
                                                        color = MaterialTheme.colorScheme.onPrimary,
                                                        maxLines = 1,
                                                        textAlign = TextAlign.Center
                                                    )

                                                    DropdownMenuForResponse(
                                                        showMenu,
                                                        friendViewModel,
                                                        userInformationViewModel,
                                                        fetchedUser!!,
                                                        homeViewModel.currentUser!!
                                                    ) { showMenu = false }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    val filterList by remember {
                        derivedStateOf {
                            val user = fetchedUser ?: return@derivedStateOf emptyList()

                            newsList.value
                                .filter { news ->
                                    news.posterId == user.uid
                                }
                                .filter { news ->
                                    when (news.decentralizationType) {
                                        DecentralizationType.Private ->
                                            news.posterId == homeViewModel.currentUser?.uid
                                        else -> true
                                    }
                                }
                        }
                    }

                    UiUtils.LazyColumnOfNewsWithSlideOutAnimationAndLoadMore(
                        localImageLoaderValue,
                        listState,
                        homeViewModel,
                        filterList,
                        onNavigateToUploadNewsfeed,
                        onNavigateToShowImageScreen,
                        onNavigateToUserInformation = {
                            //Don't allow navigate to this screen again
                        },
                        showBottomSheet = { news ->
                            newToBeShared = news
                            showBottomSheet = true
                        }
                    )
                }
                if(showBottomSheet) {
                    UiUtils.ShareBottomSheet(
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
                                    friendViewModel: FriendViewModel,
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

        @Composable
        fun DropdownMenuForCoverPhoto(expanded : Boolean,
                                      isCurrentUser : Boolean,
                                      onViewCoverPhoto : () -> Unit,
                                      onChangeCoverPhoto : () -> Unit,
                                      onDismissRequest: () -> Unit) {
            DropdownMenu(
                expanded = expanded,
                onDismissRequest = onDismissRequest
            ) {
                DropdownMenuItem(
                    text = { Text("View cover photo") },
                    onClick = {
                        onViewCoverPhoto()
                        onDismissRequest()
                    }
                )
                if(isCurrentUser) {
                    DropdownMenuItem(
                        text = { Text("Change cover photo") },
                        onClick = {
                            onChangeCoverPhoto()
                            onDismissRequest()
                        }
                    )
                }
            }
        }
    }
}