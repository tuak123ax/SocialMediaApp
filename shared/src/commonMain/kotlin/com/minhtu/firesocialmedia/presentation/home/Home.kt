package com.minhtu.firesocialmedia.presentation.home

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.minhtu.firesocialmedia.constants.TestTag
import com.minhtu.firesocialmedia.data.dto.call.OfferAnswerDTO
import com.minhtu.firesocialmedia.domain.entity.news.NewsInstance
import com.minhtu.firesocialmedia.domain.entity.user.UserInstance
import com.minhtu.firesocialmedia.platform.CrossPlatformIcon
import com.minhtu.firesocialmedia.platform.generateImageLoader
import com.minhtu.firesocialmedia.platform.logMessage
import com.minhtu.firesocialmedia.platform.toHex
import com.minhtu.firesocialmedia.presentation.loading.Loading
import com.minhtu.firesocialmedia.presentation.loading.LoadingViewModel
import com.minhtu.firesocialmedia.utils.NavigationHandler
import com.minhtu.firesocialmedia.utils.UiUtils
import com.seiko.imageloader.LocalImageLoader
import com.seiko.imageloader.ui.AutoSizeImage
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChanged

class Home {
    companion object{
        @Composable
        fun HomeScreen(modifier: Modifier,
                       homeViewModel: HomeViewModel,
                       loadingViewModel: LoadingViewModel,
                       navigateToCallingScreen : Boolean,
                       paddingValues: PaddingValues,
                       onNavigateToUploadNews: (updateNew : NewsInstance?) -> Unit,
                       onNavigateToShowImageScreen: (image : String) -> Unit,
                       onNavigateToSearch: () -> Unit,
                       onNavigateToSignIn: () -> Unit,
                       onNavigateToUserInformation: (user: UserInstance?) -> Unit,
                       onNavigateToCommentScreen: (selectedNew : NewsInstance) -> Unit,
                       onNavigateToCallingScreen : suspend (sessionId : String, caller : String, callee : String, offer: OfferAnswerDTO, ) -> Unit,
                       onNavigateToCallingScreenWithUI : suspend () -> Unit,
                       navHandler : NavigationHandler){
            val isLoading by loadingViewModel.isLoading.collectAsState()
            val commentStatus by homeViewModel.commentStatus.collectAsState()

            val showDialog = remember { mutableStateOf(false) }
            UiUtils.Companion.ShowAlertDialogToLogout(
                onClickConfirm = {
                    homeViewModel.clearAccountInStorage()
                },
                onNavigateToSignIn,
                showDialog
            )


            //Observe Live Data as State
            val usersList by  homeViewModel.allUserFriends.collectAsState()

            val numberOfLists by remember { derivedStateOf { homeViewModel.numberOfListNeedToLoad } }

            val newsList = homeViewModel.allNews.collectAsState()

            val currentUserState = homeViewModel.currentUserState

            var isAllUsersVisible by remember { mutableStateOf(true) }

            LaunchedEffect(Unit) {
                loadingViewModel.showLoading()
                //Load users list and news list.
                homeViewModel.getCurrentUserAndFriends()
                homeViewModel.getLatestNews()
                homeViewModel.getAllNotificationsOfUser()
                homeViewModel.decreaseNumberOfListNeedToLoad(1)
                if (numberOfLists == 0) {
                    loadingViewModel.hideLoading()
                }
            }
            LaunchedEffect(newsList.value) {
                homeViewModel.decreaseNumberOfListNeedToLoad(1)
                if (numberOfLists == 0) {
                    loadingViewModel.hideLoading()
                }
            }
            LaunchedEffect(commentStatus) {
                commentStatus?.let { selectedNew ->
                    onNavigateToCommentScreen(selectedNew)
                    homeViewModel.resetCommentStatus()
                }
            }

            val getCurrentUserStatus by homeViewModel.getCurrentUserStatus
            LaunchedEffect(getCurrentUserStatus) {
                if(getCurrentUserStatus) {
                    logMessage("observePhoneCall", { "start observe phone call" })
                    homeViewModel.observePhoneCall(
                        onNavigateToCallingScreen,
                        onNavigateBack = {
                            logMessage("NavStack",
                                { "Current destination: ${navHandler.getCurrentRoute()}" })
                            logMessage("observePhoneCall", { "onNavigateBack" })
                            navHandler.navigateBack()
                        })
                    if(navigateToCallingScreen) {
                        logMessage("navigateToCallingScreen", { "onNavigateToCallingScreenWithUI" })
                        onNavigateToCallingScreenWithUI()
                    }
                }
            }

            Box(modifier = Modifier.Companion
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)) {
                Column(
                    verticalArrangement = Arrangement.Top,
                    modifier = modifier.padding(paddingValues)
                ) {
                    //App name and buttons
                    Row(
                        horizontalArrangement = Arrangement.Start, modifier = Modifier.Companion
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 12.dp)
                    ) {
                        Text(
                            text = "FireSocialMedia",
                            color = MaterialTheme.colorScheme.primary,
                            style = MaterialTheme.typography.titleLarge,
                            textAlign = TextAlign.Companion.Center,
                        )
                        Spacer(modifier = Modifier.Companion.weight(1f))
                        Box(
                            modifier = Modifier.Companion
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primaryContainer)
                                .clickable {
                                    onNavigateToSearch()
                                },
                            contentAlignment = Alignment.Companion.Center
                        ) {
                            CrossPlatformIcon(
                                icon = "search",
                                backgroundColor = MaterialTheme.colorScheme.primaryContainer.toHex(),
                                contentDescription = "Search Icon",
                                contentScale = ContentScale.Companion.Fit,
                                modifier = Modifier.Companion
                                    .size(22.dp)
                                    .testTag(TestTag.Companion.TAG_ICON_BUTTON_SEARCH)
                                    .semantics {
                                        contentDescription =
                                            TestTag.Companion.TAG_ICON_BUTTON_SEARCH
                                    }
                            )
                        }

                        Spacer(modifier = Modifier.Companion.width(15.dp))

                        Box(
                            contentAlignment = Alignment.Companion.Center,
                            modifier = Modifier.Companion
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.errorContainer)
                                .clickable {
                                    showDialog.value = true
                                }
                        ) {
                            CrossPlatformIcon(
                                icon = "logout",
                                backgroundColor = MaterialTheme.colorScheme.errorContainer.toHex(),
                                contentDescription = "Logout Icon",
                                contentScale = ContentScale.Companion.Fit,
                                modifier = Modifier.Companion
                                    .size(22.dp)
                                    .testTag(TestTag.Companion.TAG_ICON_BUTTON_LOGOUT)
                                    .semantics {
                                        contentDescription =
                                            TestTag.Companion.TAG_ICON_BUTTON_LOGOUT
                                    }
                            )
                        }
                    }

                    //Create post bar and other users
                    AnimatedVisibility(visible = isAllUsersVisible) {
                        Column(verticalArrangement = Arrangement.Top) {
                            Row(
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.Companion.CenterVertically,
                                modifier = Modifier.Companion.fillMaxWidth()
                            ) {
                                //Current user avatar
                                if (currentUserState != null) {
                                    val userImage = currentUserState.image // Avoid force unwrapping

                                    CompositionLocalProvider(
                                        LocalImageLoader provides remember { generateImageLoader() },
                                    ) {
                                        AutoSizeImage(
                                            userImage,
                                            contentDescription = "Poster Avatar",
                                            contentScale = ContentScale.Companion.Crop,
                                            modifier = Modifier.Companion
                                                .size(60.dp)
                                                .padding(vertical = 5.dp)
                                                .padding(start = 10.dp)
                                                .clip(CircleShape)
                                                .clickable {
                                                    onNavigateToUserInformation(homeViewModel.currentUser)
                                                }
                                                .testTag(TestTag.Companion.TAG_CURRENT_USER)
                                                .semantics {
                                                    contentDescription =
                                                        TestTag.Companion.TAG_CURRENT_USER
                                                }
                                        )
                                    }
                                }

                                //Create post
                                OutlinedTextField(
                                    value = "",
                                    onValueChange = { },
                                    modifier = Modifier.Companion
                                        .fillMaxWidth()
                                        .padding(horizontal = 12.dp, vertical = 10.dp)
                                        .clickable { onNavigateToUploadNews(null) }
                                        .testTag(TestTag.Companion.TAG_CREATE_POST)
                                        .semantics { contentDescription = TestTag.Companion.TAG_CREATE_POST },
                                    placeholder = { Text(text = "What are you thinking?", color = MaterialTheme.colorScheme.onSurfaceVariant) },
                                    enabled = false,    // Disables the TextField
                                    singleLine = true,
                                    shape = RoundedCornerShape(28.dp)
                                )
                            }
                            LazyRow(
                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                                modifier = Modifier.Companion
                                    .fillMaxWidth()
                                    .padding(10.dp)
                                    .testTag(TestTag.Companion.TAG_USERS_ROW)
                                    .semantics {
                                        contentDescription = TestTag.Companion.TAG_USERS_ROW
                                    }
                            ) {
                                usersList.forEach { user ->
                                    if(user != null) {
                                        item {
                                            UserCard(
                                                user = user,
                                                onNavigateToUserInformation = onNavigateToUserInformation
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    val listState = rememberLazyListState()
                    // LaunchedEffect to track the scroll state (hide top bar and show load more)
                    LaunchedEffect(listState) {
                        snapshotFlow {
                            val layoutInfo = listState.layoutInfo
                            val firstVisible = layoutInfo.visibleItemsInfo.firstOrNull()?.index ?: 0
                            val lastVisible = layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0
                            val totalItems = layoutInfo.totalItemsCount

                            Triple(firstVisible, lastVisible, totalItems)
                        }
                            .distinctUntilChanged()
                            .collectLatest { (firstVisible, lastVisible, totalItems) ->
                                // Show/hide top bar
                                isAllUsersVisible = firstVisible == 0

                                // Trigger load more when near bottom
                                if (
                                    firstVisible > 0 &&
                                    lastVisible >= totalItems - 3 &&
                                    !homeViewModel.isLoadingMore.value &&
                                    homeViewModel.hasMoreData.value
                                ) {
                                    homeViewModel.loadMoreNews()
                                }
                            }
                    }

                    //Newsfeed
                    UiUtils.Companion.LazyColumnOfNewsWithSlideOutAnimationAndLoadMore(
                        listState,
                        homeViewModel,
                        newsList.value.toList(),
                        onNavigateToUploadNews,
                        onNavigateToShowImageScreen,
                        onNavigateToUserInformation
                    )
                }
                if (isLoading) {
                    Loading.Companion.LoadingScreen()
                }
            }
        }

        @Composable
        private fun UserCard(user: UserInstance, onNavigateToUserInformation: (user: UserInstance) -> Unit) {
            Card(
                modifier = Modifier.Companion.size(70.dp, 90.dp)
                    .testTag(TestTag.Companion.TAG_ITEM_IN_ROW)
                    .semantics {
                        contentDescription = TestTag.Companion.TAG_ITEM_IN_ROW
                    },
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.Companion.CenterHorizontally,
                    modifier = Modifier.Companion.fillMaxSize()
                ) {
                    CompositionLocalProvider(
                        LocalImageLoader provides remember { generateImageLoader() },
                    ) {
                        AutoSizeImage(
                            user.image,
                            contentDescription = "User Avatar",
                            contentScale = ContentScale.Companion.Crop,
                            modifier = Modifier.Companion
                                .weight(1f) // Allocates equal space to the image and text
                                .clip(CircleShape)
                                .clickable {
                                    // Handle image click
                                    onNavigateToUserInformation(user)
                                }
                        )
                    }
                    Spacer(modifier = Modifier.Companion.height(1.dp)) // Optional spacing between image and text
                    Text(
                        text = user.name,
                        color = Color.Companion.Black,
                        maxLines = 1,
                        textAlign = TextAlign.Companion.Center,
                        overflow = TextOverflow.Companion.Ellipsis, // Adds "..." at the end if the text overflows
                        modifier = Modifier.Companion.padding(horizontal = 4.dp) // Adds padding around text
                    )
                }
            }
        }

        fun getScreenName(): String{
            return "HomeScreen"
        }
    }

}